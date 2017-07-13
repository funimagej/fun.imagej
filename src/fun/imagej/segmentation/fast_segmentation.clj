(ns fun.imagej.segmentation.fast-segmentation
  (:require [fun.imagej.img :as img]
            [fun.imagej.core :as imagej]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.ops :as ops])
  (:import [org.apache.commons.math3.linear QRDecomposition Array2DRowRealMatrix ArrayRealVector SingularValueDecomposition]))

;; The Fast Segmentation paradigm uses a hash map as the base data structure

(defn create-segmentation-meta
  "Create the metadata for a fast segmentation"
  ([]
    (create-segmentation-meta {:segmentation-type :2D}))
  ([seg]
    (assoc seg
           :weights         []
           :feature-map-fns [])))

(defn add-feature-map-fn
  "Add a feature map generator to a segmentation. These are stored as maps"
  [seg feature-name feature-fn]
  (assoc seg
         :feature-map-fns
         (conj (:feature-map-fns seg)
               {:name feature-name
                :fn   feature-fn})))

(defn generate-position
  "Generate a candidate sample position.
We should probably give a way of providing a custom dimension ordering."
  [seg label]
  (cond (= (:segmentation-type seg) :3D)
        ^longs (long-array [(rand-int (img/get-width label))
                            (rand-int (img/get-height label))
                            (rand-int (img/get-size-dimension label 2))])
        (= (:segmentation-type seg) :2D)
        ^longs (long-array [(rand-int (img/get-width label))
                            (rand-int (img/get-height label))])
        :else
        (println "We should autodetect here")))

(defn generate-sample-points
  "Generate the positive and negative sample points given a segmentation and the target labeling.
      Positive samples are drawn from the labeling, while negative samples come from regions outside the labeling."
  [seg label]
  ;(println "generate-sample-points " (class label))
  (loop [seg (assoc seg
                    :positive-samples []
                    :negative-samples [])]
    (if (or (< (count (:positive-samples seg))
               (:num-positive-samples seg))
            (< (count (:negative-samples seg))
               (:num-negative-samples seg)))      
      (let [candidate-pos (generate-position seg label)
            candidate-val (img/get-val ^net.imglib2.img.imageplus.ByteImagePlus label ^longs candidate-pos)]
        ;(println "Positive " (count (:positive-samples seg)) " Negative " (count (:negative-samples seg)) " val " candidate-val)
           (cond                                    ; True, and need positive samples
             (and candidate-val
                  (< (count (:positive-samples seg)) (:num-positive-samples seg)))
             (do
               (when (:verbose seg)
                     (println "pos:" (count (:positive-samples seg))
                              "neg:" (count (:negative-samples seg))))
               (recur (assoc seg
                             :positive-samples (conj (:positive-samples seg) candidate-pos))))
             ; False, and need negative samples
             (and (or (not candidate-val)
                      (zero? candidate-val))
                  (< (count (:negative-samples seg)) (:num-negative-samples seg)))
             (do
               (when (:verbose seg)
                     (println "pos:" (count (:positive-samples seg))
                              "neg:" (count (:negative-samples seg))))
               (recur (assoc seg
                             :negative-samples (conj (:negative-samples seg) candidate-pos))))
             :else
             (recur seg)))
      seg)))

;; Maybe deprecate
(defn generate-sample-points-negative-labels
  "Generate the positive and negative sample points given a segmentation and the target labeling.
      Positive samples are drawn from the labeling, while negative samples come from regions outside the labeling."
  [seg label negative-labels]
  (loop [seg seg]
    (if (or (< (count (:positive-samples seg))
               (:num-positive-samples seg))
            (< (count (:negative-samples seg))
               (:num-negative-samples seg)))
      (let [candidate-pos (generate-position seg label)
            candidate-val (img/get-val label candidate-pos)]
        #_(println candidate-val)
        #_(println (map #(img/get-val % candidate-pos)
                                     negative-labels)
                  (reduce #(or %1 %2) (map #(img/get-val % candidate-pos)
                                           negative-labels)))
           (cond                                    ; True, and need positive samples
             (and (or (and (number? candidate-val) (pos? candidate-val))
                      (and (not (number? candidate-val)) candidate-val))
                  (< (count (:positive-samples seg)) (:num-positive-samples seg)))
             (do
               (when (:verbose seg)
                     (println "pos:" (count (:positive-samples seg))
                              "neg:" (count (:negative-samples seg))))
               (recur (assoc seg
                             :positive-samples (conj (:positive-samples seg) candidate-pos))))
             ; False, and need negative samples
             (and (and (not candidate-val)
                       (reduce #(or %1 %2)
                               (map #(img/get-val % candidate-pos)
                                    negative-labels)))
                  (< (count (:negative-samples seg)) (:num-negative-samples seg)))
             (do
               (when (:verbose seg)
                     (println "pos:" (count (:positive-samples seg))
                              "neg:" (count (:negative-samples seg))))
               (recur (assoc seg
                             :negative-samples (conj (:negative-samples seg) candidate-pos))))
             :else
             (recur seg)))
      seg)))

(defn generate-sample-points-negative-label
  "Generate the positive and negative sample points given a segmentation and the target labeling.
      Positive samples are drawn from the labeling, while negative samples come from regions outside the labeling."
  [seg label negative-label]
  (loop [seg seg]
    (if (or (< (count (:positive-samples seg))
               (:num-positive-samples seg))
            (< (count (:negative-samples seg))
               (:num-negative-samples seg)))
      (let [candidate-pos (generate-position seg label)
            candidate-val (img/get-val label candidate-pos)
            negative-candidate-val (img/get-val negative-label candidate-pos)]
        #_(println candidate-val
                  (or (and (number? candidate-val) (pos? candidate-val))
                       (and (not (number? candidate-val)) candidate-val))
                  negative-candidate-val
                  (or (and (number? negative-candidate-val) (pos? negative-candidate-val))
                      (and (not (number? negative-candidate-val)) negative-candidate-val)))
        #_(println (map #(img/get-val % candidate-pos)
                                     negative-labels)
                  (reduce #(or %1 %2) (map #(img/get-val % candidate-pos)
                                           negative-labels)))
           (cond                                    ; True, and need positive samples
             (and (or (and (number? candidate-val) (pos? candidate-val))
                      (and (not (number? candidate-val)) candidate-val))
                  (< (count (:positive-samples seg)) (:num-positive-samples seg)))
             (do
               (when (:verbose seg)
                     (println "pos:" (count (:positive-samples seg))
                              "neg:" (count (:negative-samples seg))))
               (recur (assoc seg
                             :positive-samples (conj (:positive-samples seg) candidate-pos))))
             ; False, and need negative samples
             (and (or (and (number? negative-candidate-val) (pos? negative-candidate-val))
                      (and (not (number? negative-candidate-val)) negative-candidate-val))
                  (< (count (:negative-samples seg)) (:num-negative-samples seg)))
             (do
               (when (:verbose seg)
                     (println "pos:" (count (:positive-samples seg))
                              "neg:" (count (:negative-samples seg))))
               (recur (assoc seg
                             :negative-samples (conj (:negative-samples seg) candidate-pos))))
             :else
             (recur seg)))
      seg)))

(defn generate-dataset
  "Generate a dataset for training"
  [seg input-img]
  (loop [seg (assoc seg
                    :feature-vals []
                    :target-vals (concat (repeat (count (:positive-samples seg)) 1)
                                         (repeat (count (:negative-samples seg)) 0)))
         feature-map-fns (:feature-map-fns seg)]    
    (if (empty? feature-map-fns)
      seg
      (let [feature-name (:name (first feature-map-fns))
            _ (when (:verbose seg) (println "generate-dataset working on" feature-name ))
            feature-map-fn (:fn (first feature-map-fns))
            feature-map (fun.imagej.ops.convert/float32
                          (if (and
                                (:cache-directory seg)
                                (.exists (java.io.File. (str (:cache-directory seg)
                                                             (:basename seg) "_"
                                                             feature-name ".tif"))))
                            (imagej/open-img (str (:cache-directory seg) (:basename seg) "_" feature-name ".tif"))
                            (feature-map-fn input-img)))]
        (when (:verbose seg) (println "feature map generated."))
        (when (:cache-directory seg)
          (imagej/save-img feature-map (str (:cache-directory seg) (:basename seg) "_" feature-name ".tif")))
        (recur (assoc seg
                      :feature-vals
                      (conj (:feature-vals seg)
                            (doall
                              (map #(img/get-val feature-map %)
                                   (concat (:positive-samples seg)
                                           (:negative-samples seg))))))
               (rest feature-map-fns))))))

(defn solve-segmentation
  "Solve the segmentation for fully populated metadata."
  [seg]
  (let [data-matrix (into-array
                      (map double-array
                           (apply (partial map list)
                                  (:feature-vals seg))))
        coefficients (org.apache.commons.math3.linear.Array2DRowRealMatrix. data-matrix false)
        ;solver (.getSolver (org.apache.commons.math3.linear.QRDecomposition. coefficients))
        solver (.getSolver (org.apache.commons.math3.linear.SingularValueDecomposition. coefficients))
        
        constants (org.apache.commons.math3.linear.ArrayRealVector. (double-array (:target-vals seg)) false)
        solution (.solve solver constants)]
    (assoc seg
           :weights (doall 
                      (map #(.getEntry solution %)
                           (range (.getDimension solution)))))))

(defn segment-image
  "Use a solved segmentation and an input image to segment an input."
  [seg to-segment]
  (let [solution-img
        (fun.imagej.ops.convert/float32 (img/create-img-like to-segment))]    
    (dotimes [k (count (:feature-map-fns seg))]
      (let [ffmap (nth (:feature-map-fns seg) k)
            feature-name (:name ffmap)
            feature-map-fn (:fn ffmap)
            feature-map (fun.imagej.ops.convert/float32
                          (if (and
                                (:cache-directory seg)
                                (.exists (java.io.File. (str (:cache-directory seg)
                                                             (:cache-basename seg) "_"
                                                             feature-name ".tif"))))
                            (imagej/open-img (str (:cache-directory seg) (:basename seg) "_" feature-name ".tif"))
                            (feature-map-fn to-segment)))]
        (when (:verbose seg) (println feature-name (str (:cache-directory seg) (:basename seg) "_" feature-name ".tif")))
        (fun.imagej.ops.math/add solution-img
                                      solution-img
                                      (fun.imagej.ops.math/multiply (fun.imagej.ops.convert/float32 feature-map)
                                                                    ^float (float (nth (:weights seg) k))))))
    (when (:cache-directory seg)
      (imagej/save-img solution-img (str (:cache-directory seg) (:basename seg) "_segmentation.tif")))
    solution-img))

(defn save-segmentation-config
  "Save a clean version of the segmentation configuration."
  [seg filename]
  (spit filename
        {:weights seg
         :positive-samples seg
         :negative-samples seg
         :feature-vals seg
         :num-positive-samples seg
         :num-negative-samples seg}))
         
(defn clear-cache
  "Clear the cache we created for our featuremaps"
  [seg]
  (dotimes [k (count (:feature-map-fns seg))]
      (let [ffmap (nth (:feature-map-fns seg) k)
            feature-name (:name ffmap)
            feature-map-fn (:fn ffmap)]
        (when (:cache-directory seg)
          (.delete (java.io.File. (str (:cache-directory seg) (:cache-basename seg) "_" feature-name ".tif")))))))
