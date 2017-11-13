; @Dataset(label="2D Timeseries Image",description="2D timeseries image") input

(ns fun.imagej.test.experimental.branched-spacetime
  (:require [fun.imagej.img :as img]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.conversion :as convert]
            [fun.imagej.segmentation.imp :as ij1seg]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.imp.roi :as roi]
            [fun.imagej.img.type :as imtype]
            [fun.imagej.imp :as ij1]
            [clojure.string :as string]))

;; TODO:
; - measure a band along ROI line
; - use a branch point and roi detector

(refer 'user)

(def roi-manager (ij.plugin.frame.RoiManager/getRoiManager))

(ij.IJ/setTool "freeline")

; Select ROIs and save
(let [gd (ij.gui.NonBlockingGenericDialog. "Branch Selection")]
  (.enableYesNoCancel gd "Done" "Quit")
  (.showDialog gd)
  (loop []
    (when-not (or (.wasCanceled gd)
                  (.wasOKed gd))
      (Thread/sleep 20))))

; Collect ROIs
(def all-rois (seq (.getRoisAsArray roi-manager)))

(doseq [roi all-rois]
  (let [poly (.getFloatPolygon roi)
        xs (.xpoints poly)
        ys (.ypoints poly)
        branch-img (fun.imagej.ops.create/img (long-array [(.npoints poly)
                                                            (img/get-size-dimension input 3)]))]
    (dotimes [k (.npoints poly)]
      (dotimes [t (img/get-size-dimension input 3)]
        (let [x (aget xs k)
              y (aget ys k)]
          (img/set-val branch-img (long-array [k t])
                       (double (img/get-val input (long-array [x y 2 t])))))))
    (let [roi-imp (convert/img->imp (fun.imagej.ops.convert/float32 branch-img))]
      (.show roi-imp)
      (.updateAndRepaintWindow roi-imp))))


