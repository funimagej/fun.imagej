(ns fun.imagej.algorithm.optical-flow
  (:require [fun.imagej.img :as img]
            [fun.imagej.conversion :as convert]
            [fun.imagej.core :as ij]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.img.cursor :as cursor]
            [clojure.string :as string]
            [fun.imagej.ops])
  (:import (net.imglib2 IterableInterval Cursor Interval RandomAccessibleInterval RandomAccess)
           (net.imglib2.view Views)
           (net.imglib2.util Intervals)
           (net.imglib2.algorithm.neighborhood Neighborhood)))

(defn create-offsets-r
  "Return a sequence of offset vectors for all dimensions and given radius"
  [num-dim radius]
  (let [rg  (range (- (dec radius)) radius)]
    (loop [view-set (map vector rg)
           remaining (rest (range num-dim))]
      (if (empty? remaining)
        view-set
        (recur (doall (mapcat (fn [v]
                                (map #(concat v [%])
                                     rg))
                              view-set))
               (rest remaining))))))

(defn create-view-set-r
  "Create a list of View RAIs for each offset of an input RAI.
  Use of radius should be deprecated.
  Returns a view and a weight based on distance"
  [input radius]
  (let [offsets (create-offsets-r (img/num-dimensions input) radius)]
    (doall
      (for [offset offsets]
        [(#(- 1 (if (zero? %) (identity  %) (/ %)))
           (reduce + (map #(* % %) offset))); Weight is 1/distance^2 with div by zero check
         (net.imglib2.view.Views/offset ^RandomAccessibleInterval input ^longs (long-array offset))
         offset]))))

(defn velocity-img-3d
  "Return the velocity img in channels using an assumption that Z is the scanning axis.
  Expects a XYTZ image (1 channel)"
  [input]
  (let [time-dimension 3
        vx (fun.imagej.ops.create/img (img/hyperslice input time-dimension 0))
        vy (fun.imagej.ops.create/img (img/hyperslice input time-dimension 0))
        vz (fun.imagej.ops.create/img (img/hyperslice input time-dimension 0))

        radius 3

        destVX ^RandomAccessibleInterval (Views/interval vx (Intervals/expand vx (* -1 radius)))
        destVY ^RandomAccessibleInterval (Views/interval vy (Intervals/expand vy (* -1 radius)))
        destVZ ^RandomAccessibleInterval (Views/interval vz (Intervals/expand vz (* -1 radius)))

        ;; Will be writing to these 3 velocity maps; these should be RAIs instead
        cVX ^Cursor (.localizingCursor destVX); Will be used for localizing frequently
        cVY ^Cursor (.cursor (Views/iterable destVY))
        cVZ ^Cursor (.cursor (Views/iterable destVZ))

        ;; Shape to use, could use more general typing
        shape (shape/rectangle radius)

        ; Reading from the following
        ; What about using views to get convolutional neighbors
        ; - Use a list of RAIs (e.g. View's), then no Cursors
        T0 (Views/interval (img/hyperslice input time-dimension 0)
                           (Intervals/expand (img/hyperslice input time-dimension 0) (* -1 radius)))
        ;nbrsT0 ^IterableInterval (.neighborhoods shape T0)
        ;cNbrsT0 ^Cursor (.cursor nbrsT0) ; Create a list of RAIs instead
        raisT0 (create-view-set-r T0 radius)
        ;_ (println "raisT0" raisT0)

        time-offset 10

        T1 (Views/interval (img/hyperslice input time-dimension time-offset)
                           (Intervals/expand (img/hyperslice input time-dimension time-offset) (* -1 radius)))
        ;nbrsT1 ^IterableInterval (.neighborhoods shape T1)
        ;cNbrsT1 ^Cursor (.cursor nbrsT0)
        raisT1 (create-view-set-r T1 radius)

        ;num-steps (atom 0)
        ]
    ;(println "XXXXXX" (seq (img/dimensions vx)) (seq (img/dimensions destVX)) (seq (img/dimensions destVY)))
    (let [offset ^longs (long-array (img/num-dimensions T0))]
      (loop []
        (when (.hasNext cVX)
          (.fwd cVX) (.fwd cVY) (.fwd cVZ)
          ;Solve for velocities, then set vals for velocity map
          (.localize cVX offset)
          ;https://en.wikipedia.org/wiki/Lucas%E2%80%93Kanade_method
          (let [t0-vals (map #(* (first %);; weighting each view
                                 (img/get-val (second %) offset))
                             raisT0)
                t1-vals (map #(* (first %)
                                 (img/get-val (second %) offset))
                             raisT1)
                diffs (map - t0-vals t1-vals)

                data-matrix (into-array
                              (map double-array
                                   (map last raisT0))); use array of offsets
                coefficients (org.apache.commons.math3.linear.Array2DRowRealMatrix. data-matrix false)
                solver (.getSolver (org.apache.commons.math3.linear.SingularValueDecomposition. coefficients))
                constants (org.apache.commons.math3.linear.ArrayRealVector. (double-array diffs) false)
                solution (.solve solver constants)]
            ;(println (seq offset) t0-vals t1-vals diffs (.getEntry solution 0) (doall (map first raisT0)))
            (cursor/set-val! cVX (.getEntry solution 0))
            (cursor/set-val! cVY (.getEntry solution 1))
            (cursor/set-val! cVZ (.getEntry solution 2)))
          (recur))))
    (img/concat-imgs [vx vy vz])))

(defn velocity-img-2d
  "Return the velocity img in channels using an assumption that Z is the scanning axis.
  Expects a XYTZ image (1 channel)"
  [input time-dimension t-start t-stop]
  (let [vx (fun.imagej.ops.create/img (img/hyperslice input time-dimension t-start))
        vy (fun.imagej.ops.create/img (img/hyperslice input time-dimension t-start))

        radius 3

        destVX ^RandomAccessibleInterval (Views/interval vx (Intervals/expand vx (* -1 radius)))
        destVY ^RandomAccessibleInterval (Views/interval vy (Intervals/expand vy (* -1 radius)))

        ;; Will be writing to these 3 velocity maps; these should be RAIs instead
        cVX ^Cursor (.localizingCursor destVX); Will be used for localizing frequently
        cVY ^Cursor (.cursor (Views/iterable destVY))

        ;; Shape to use, could use more general typing
        shape (shape/rectangle radius)

        ; Reading from the following
        ; What about using views to get convolutional neighbors
        ; - Use a list of RAIs (e.g. View's), then no Cursors
        T0 (Views/interval (img/hyperslice input time-dimension 0)
                           (Intervals/expand (img/hyperslice input time-dimension 0) (* -1 radius)))
        raisT0 (create-view-set-r T0 radius)

        ;time-offset 10

        T1 (Views/interval (img/hyperslice input time-dimension t-stop)
                           (Intervals/expand (img/hyperslice input time-dimension t-stop) (* -1 radius)))
        raisT1 (create-view-set-r T1 radius)]
    (let [offset ^longs (long-array (img/num-dimensions T0))]
      (loop []
        (when (.hasNext cVX)
          (.fwd cVX) (.fwd cVY)
          ;Solve for velocities, then set vals for velocity map
          (.localize cVX offset)
          ;https://en.wikipedia.org/wiki/Lucas%E2%80%93Kanade_method
          (let [t0-vals (map #(* (first %);; weighting each view
                                 (img/get-val (second %) offset))
                             raisT0)
                t1-vals (map #(* (first %)
                                 (img/get-val (second %) offset))
                             raisT1)
                diffs (map - t0-vals t1-vals)

                data-matrix (into-array
                              (map double-array
                                   (map last raisT0))); use array of offsets
                coefficients (org.apache.commons.math3.linear.Array2DRowRealMatrix. data-matrix false)
                solver (.getSolver (org.apache.commons.math3.linear.SingularValueDecomposition. coefficients))
                constants (org.apache.commons.math3.linear.ArrayRealVector. (double-array diffs) false)
                solution (.solve solver constants)]
            ;(println (seq offset) t0-vals t1-vals diffs (.getEntry solution 0) (doall (map first raisT0)))
            (cursor/set-val! cVX (.getEntry solution 0))
            (cursor/set-val! cVY (.getEntry solution 1)))
          (recur))))
    (img/concat-imgs [vx vy])))


;; This is the incomplete ND version of Lucas-Kanade optical flow
#_(defn optical-flow
  "Return the velocity img in channels using an assumption that Z is the scanning axis.
  Expects a XYTZ image (1 channel).
  Implements the Lucas-Kanade method"
  [input time-dimension t-start t-stop]
  (let [time-dimension 3
        vs (repeatedly (dec (img/num-dimensions input))
                       #(fun.imagej.ops.create/img (img/hyperslice input time-dimension 0)))

        radius 3; should be an optional argument

        ; Stores the velocity images
        ; TODO contiue here

        destVX ^RandomAccessibleInterval (Views/interval vx (Intervals/expand vx (* -1 radius)))
        destVY ^RandomAccessibleInterval (Views/interval vy (Intervals/expand vy (* -1 radius)))
        destVZ ^RandomAccessibleInterval (Views/interval vz (Intervals/expand vz (* -1 radius)))

        ;; Will be writing to these 3 velocity maps; these should be RAIs instead
        cVX ^Cursor (.localizingCursor destVX); Will be used for localizing frequently
        cVY ^Cursor (.cursor (Views/iterable destVY))
        cVZ ^Cursor (.cursor (Views/iterable destVZ))

        ;; Shape to use, could use more general typing
        shape (shape/rectangle radius)

        ; Reading from the following
        ; What about using views to get convolutional neighbors
        ; - Use a list of RAIs (e.g. View's), then no Cursors
        T0 (Views/interval (img/hyperslice input time-dimension t-start)
                           (Intervals/expand (img/hyperslice input time-dimension t-start) (* -1 radius)))
        raisT0 (create-view-set-r T0 radius)
        ;_ (println "raisT0" raisT0)

        T1 (Views/interval (img/hyperslice input time-dimension t-stop)
                           (Intervals/expand (img/hyperslice input time-dimension t-stop) (* -1 radius)))
        raisT1 (create-view-set-r T1 radius)]

    (let [offset ^longs (long-array (img/num-dimensions T0))]
      (loop []
        (when (.hasNext cVX)
          (.fwd cVX) (.fwd cVY) (.fwd cVZ)
          (.localize cVX offset)
          (let [t0-vals (map #(* (first %);; weighting each view
                                 (img/get-val (second %) offset))
                             raisT0)
                t1-vals (map #(* (first %)
                                 (img/get-val (second %) offset))
                             raisT1)
                diffs (map - t0-vals t1-vals)

                data-matrix (into-array
                              (map double-array
                                   (map last raisT0))); use array of offsets
                coefficients (org.apache.commons.math3.linear.Array2DRowRealMatrix. data-matrix false)
                solver (.getSolver (org.apache.commons.math3.linear.SingularValueDecomposition. coefficients))
                constants (org.apache.commons.math3.linear.ArrayRealVector. (double-array diffs) false)
                solution (.solve solver constants)]
            (cursor/set-val! cVX (.getEntry solution 0))
            (cursor/set-val! cVY (.getEntry solution 1))
            (cursor/set-val! cVZ (.getEntry solution 2)))
          (recur))))
    (img/concat-imgs [vx vy vz])))
