(ns fun.imagej.test.presentations.dd2017
  (:require [fun.imagej.img :as img]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.conversion :as convert]
            [fun.imagej.segmentation.imp :as ij1seg]
            [fun.imagej.segmentation.fast-segmentation :as fseg]
            [fun.imagej.imp.roi :as roi]
            [fun.imagej.img.type :as imtype]
            [fun.imagej.imp :as ij1]
            [clojure.test :refer :all]
            [fun.imagej.imp :as imp]
            [fun.imagej.img.cursor :as cursor]))

; Open with ImageJ2

(def my-img (first (img/dimension-split (ij/open-img "http://mirror.imagej.net/images/blobs.gif")
                                        2)))
(ij/show my-img "My image")
(ij/show-ui)

; ImageJ1 usage (but why would you?)

(def my-imp (convert/img->imp my-img))
(imp/show-imp my-imp)

; Ops usage

(def bin-img (fun.imagej.ops.threshold/rosin my-img))
(ij/show bin-img)

; Try all global threshold methods, because rosin isn't what we want

(def threshold-methods (filter #(and (not= 'apply (first %))
                                     (not (.contains (name (first %)) "local")))
                               (ns-interns 'fun.imagej.ops.threshold)))

(doseq [method threshold-methods]; Loop over all threshold methods that are not apply or local thresholders
  (let [bin-img ((second method) my-img)]
    (ij/show bin-img (name (first method)))))

; That was annoying, maybe it would be easier to look at a tiled image
(imp/show-imp (imp/tile-imps (map #(convert/img->imp ((second %) my-img))
                                  threshold-methods)))

; Now let's choose a binary image to work on
(def bin-img (first
               (img/map-img #(cursor/set-val % (not (cursor/get-val %)))
                          (fun.imagej.ops.threshold/huang my-img))))
(ij/show bin-img "Binary img")

; Let's do some IJ1 style particle analysis
(let [this-imp (convert/img->imp bin-img)
      my-rois (ij1seg/imp-to-rois this-imp)]
  (println (count my-rois))
  (let [area-threshold (nth (sort (map roi/area my-rois))
                            (int (* 0.75 (count my-rois))))]
    (println "Area threshold:" area-threshold)
    (def target-img
        (ij/show (convert/imp->img (ij1seg/rois-to-imp (imp/create-imp-like this-imp)
                                                       (filter #(> (roi/area %) area-threshold)
                                                               my-rois)))
                 "Filtered ROIs"))))

; Let's try some segmentation
(def segmentor
      (-> (fseg/create-segmentation-meta {:num-positive-samples 2000
                                          :num-negative-samples 2000
                                          :basename "mySegmentation"
                                          :verbose true
                                          :segmentation-type :2D})
          ;; Add some feature maps for the segmentation
          (fseg/add-feature-map-fn "raw" (fn [target] (fun.imagej.ops.convert/float32 (img/copy target))))
          (fseg/add-feature-map-fn "sqgradient_x" (fn [target]
                                                    (let [grad (img/gradient target 0)]
                                                      (fun.imagej.ops.math/multiply grad grad))))
          (fseg/add-feature-map-fn "sqgradient_y" (fn [target]
                                                    (let [grad (img/gradient target 1)]
                                                      (fun.imagej.ops.math/multiply grad grad))))
          (fseg/add-feature-map-fn "hessian_0" (fn [target]
                                                 (let [grads (img/concat-imgs [(img/gradient target 0) (img/gradient target 1)])
                                                       hessians (img/hessian-matrix grads)]
                                                   (img/hyperslice hessians 2 0))))
          (fseg/add-feature-map-fn "hessian_1" (fn [target]
                                                 (let [grads (img/concat-imgs [(img/gradient target 0) (img/gradient target 1)])
                                                       hessians (img/hessian-matrix grads)]
                                                   (img/hyperslice hessians 2 1))))
          (fseg/add-feature-map-fn "eigen_0" (fn [target]
                                               (let [grads (img/concat-imgs [(img/gradient target 0) (img/gradient target 1)])
                                                     eigens (img/tensor-eigen-values (img/hessian-matrix grads))]
                                                 (img/hyperslice eigens 2 0))))
          (fseg/add-feature-map-fn "eigen_1" (fn [target]
                                               (let [grads (img/concat-imgs [(img/gradient target 0) (img/gradient target 1)])
                                                     eigens (img/tensor-eigen-values (img/hessian-matrix grads))]
                                                 (img/hyperslice eigens 2 1))))
          (fseg/add-feature-map-fn "medianFilter10sphere" (fn [target] (let [output (img/create-img-like target)]
                                                                         (fun.imagej.ops.filter/median output
                                                                                                       target
                                                                                                       (shape/sphere-shape 10))
                                                                         (fun.imagej.ops.convert/float32 output))))
          (fseg/add-feature-map-fn "medianFilter20sphere" (fn [target] (let [output (img/create-img-like target)]
                                                                         (fun.imagej.ops.filter/median output
                                                                                                       target
                                                                                                       (shape/sphere-shape 20))
                                                                         (fun.imagej.ops.convert/float32 output))))
          (fseg/add-feature-map-fn "varianceFilter10sphere" (fn [target] (let [output (img/create-img-like target)]
                                                                         (fun.imagej.ops.filter/variance output
                                                                                                       target
                                                                                                       (shape/sphere-shape 10))
                                                                         (fun.imagej.ops.convert/float32 output))))
          (fseg/add-feature-map-fn "varianceFilter20sphere" (fn [target] (let [output (img/create-img-like target)]
                                                                         (fun.imagej.ops.filter/variance output
                                                                                                       target
                                                                                                       (shape/sphere-shape 20))
                                                                         (fun.imagej.ops.convert/float32 output))))
          ;; Generate the sample points that will be used for training
          (fseg/generate-sample-points target-img)
          ;; Generate the training dataset
          (fseg/generate-dataset (fun.imagej.ops.convert/float32 my-img))
          (fseg/solve-segmentation)))

(def segmented-image (ij/show (fseg/segment-image segmentor (fun.imagej.ops.convert/float32 my-img))
                              "Segmented image"))

(ij/show (fun.imagej.ops.threshold/huang segmented-image))

;; SciView


