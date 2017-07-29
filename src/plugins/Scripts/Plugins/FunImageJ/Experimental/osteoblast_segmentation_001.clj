(ns fun.imagej.img.osteoblast-segmentation-001
  (:import [net.imglib2.algorithm.gauss3 Gauss3]
           [net.imglib2.converter Converters RealDoubleConverter]
           [net.imglib2.view Views])
  (:require [fun.imagej.img :as img]
            [fun.imagej.img.type :as tpe]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.segmentation.fast-segmentation :as fseg]))

(defn smooth
  "Smooth an image with a Gaussian."
  [input]  
  (let [output (fun.imagej.ops.create/img input)
        #_(img/create-img-like input)]
    (fun.imagej.ops.filter/gauss output input (double-array [5 5]))
    #_(Gauss3/gauss 5 (Views/extendBorder input) output)
    output))

(defn watershed-seeds
  "Return an image of the watershed seeds."
  [input]
  (let [dinput (fun.imagej.ops.convert/float32 input)
        smoothed (smooth dinput)
        gradients (img/concat-imgs
                    (map (partial img/gradient smoothed)
                         (range 2)))]
    (img/tensor-eigen-values 
      (img/hessian-matrix gradients))))

#_(let [input (ij/open-img "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004.tif")
       input (map (partial img/hyperslice input 2)
                  (range (img/get-size-dimension input 2)))
       out-filename "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004_seeds.tif"
       seeds (img/concat-imgs (map watershed-seeds input))]
   (println seeds)
   (ij/save-img (fun.imagej.ops.convert/float32 seeds) out-filename)
   (img/show seeds))

(def input (ij/open-img "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004.tif"))
(def target (fun.imagej.ops.threshold/intermodes 
              (fun.imagej.ops.convert/float32
                          (ij/open-img "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004_positiveMask.tif"))))
  
(def seg (atom (fseg/create-segmentation-meta {:num-positive-samples 2000
                                               :num-negative-samples 2000
                                               :basename "TileScan_004"
                                               :cache-directory "/Users/kharrington/Data/Tabler_Jacqui/cache/"
                                               :segmentation-type :3D})))

(reset! seg (-> @seg
              (fseg/add-feature-map-fn "raw" (fn [target] (img/copy target))) ; Raw image as feature map
              #_(fseg/add-feature-map-fn "isodataThresh" (fn [target] (fun.imagej.ops.threshold/isoData (img/copy target))))
              #_(fseg/add-feature-map-fn "tophat5" (fn [target] (fun.imagej.ops.morphology/topHat (img/copy target) [(shape/sphere-shape 3)])))
              #_(fseg/add-feature-map-fn "medianFilter3rect" (fn [target] (let [output (img/create-img-like target)]
                                                             (fun.imagej.ops.filter/median output
                                                                                                target
                                                                                                (shape/sphere-shape 3))
                                                             output)))
              #_(fseg/add-feature-map-fn "medianFilter5rect" (fn [target] (let [output (img/create-img-like target)]
                                                             (fun.imagej.ops.filter/median output
                                                                                                target
                                                                                                (shape/sphere-shape 5))
                                                             output)))
              #_(fseg/add-feature-map-fn "varianceFilter3rect" (fn [target] (let [output (img/create-img-like target)]
                                                               (fun.imagej.ops.filter/variance output
                                                                                                    target
                                                                                                    (shape/sphere-shape 3))
                                                               output)))
              (fseg/add-feature-map-fn "gradient-x" (fn [target] 
                                                      (img/gradient target 0)))
              (fseg/add-feature-map-fn "gradient-y" (fn [target] 
                                                      (img/gradient target 1)))
              #_(fseg/add-feature-map-fn "varianceFilter9rect" (fn [target] (let [output (img/create-img-like target)]
                                                               (fun.imagej.ops.filter/variance output
                                                                                                    target
                                                                                                    (shape/sphere-shape 9))
                                                               output)))
              (fseg/add-feature-map-fn "dog_1_1.25" (fn [target] (let [output (fun.imagej.ops.convert/float32 (img/create-img-like target))]
                                                                   (fun.imagej.ops.filter/dog output
                                                                                              (fun.imagej.ops.convert/float32 (img/copy target))
                                                                                                   1.0 1.25)
                                                                   output)))))

(println "Generating sample points.")
(reset! seg (fseg/generate-sample-points @seg (fun.imagej.ops.convert/uint8 target)))

(println "Generating dataset.")
(reset! seg (fseg/generate-dataset @seg input))

(println "Solving segmentation")
(reset! seg (fseg/solve-segmentation @seg))
      
(println "Writing segmentation map")
(let [segmentation (fseg/segment-image @seg input)]
  (ij/show segmentation "Segmentation")
  (ij/show target "Target")
  (ij/save-img segmentation
               "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004_fsegMap.tif"))     
