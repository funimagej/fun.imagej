(ns plugins.Scripts.Plugins.fun.imagej.Experimental.osteoblast-segmentation-002
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
	  (let [output (fun.imagej.ops.create/img input)]
	    (fun.imagej.ops.filter/gauss output input (double-array [5 5 0]))
	    output))

(defn copy-through-mask
  "Create a copy of an image through a mask."
  [im mask]
  (let [out (fun.imagej.ops.create/img (img/dimensions im))]
    (first
      (img/map-img (fn [^net.imglib2.Cursor cur1 ^net.imglib2.Cursor cur2 ^net.imglib2.Cursor cur3]
                     (if (.get ^net.imglib2.type.logic.BitType (.get cur2))                   
                       (cursor/copy cur1 cur3)
                       (cursor/set-zero cur1)))
                   out mask im))))

(defn cell-borders
	  "Return an image of the cell borders."
	  [input]
	  (let [dinput (fun.imagej.ops.convert/float32 input)
	        smoothed (smooth dinput)
	        gradients (img/concat-imgs
	                    (map (partial img/gradient smoothed)
	                         (range 2)))]
	    (first (img/dimension-split
	             (img/tensor-eigen-values 
	               (img/hessian-matrix gradients))
	             3))))

(defn nth-eigen
   [input n]
   (let [dinput (fun.imagej.ops.convert/float32 input)
	       smoothed (smooth dinput)
	       gradients (img/concat-imgs
	                   (map (partial img/gradient smoothed)
	                        (range 2)))]
	    (nth (img/dimension-split
	           (img/tensor-eigen-values 
	             (img/hessian-matrix gradients))
	           3) n))) 

(defn -main
  [& args]
	
	
	(def input (ij/open-img "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004.tif"))
	;(def positive-target (cell-borders input))
	
	(ij/show-ui)
 
	(def positive-negative
	  (fun.imagej.ops.threshold/otsu 
	              (fun.imagej.ops.filter/gauss
	                (fun.imagej.ops.convert/float32
	                  (img/copy input))
	                (double-array [5 5 0]))))
	(def negative-target  
	  (fun.imagej.ops.convert/uint8
	    (fun.imagej.ops.image/invert
	     (img/create-img-like positive-negative)
	     positive-negative)))
	(println "Constructed negative target")
	(ij/save-img negative-target "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004_negativeTarget.tif")
	(ij/show negative-target "Negative target")
	
	(def borders (cell-borders input))
	(def positive-target (fun.imagej.ops.convert/uint8
	                       (let [initial (fun.imagej.ops.threshold/intermodes
	                                       (copy-through-mask
	                                         (fun.imagej.ops.image/invert
	                                           (fun.imagej.ops.create/img (img/dimensions borders))
	                                           (fun.imagej.ops.math/multiply
	                                             (fun.imagej.ops.image/invert
	                                               (fun.imagej.ops.create/img (img/dimensions borders))             
	                                               borders)
	                                             (fun.imagej.ops.convert/float64 input)))
	                                         positive-negative))]                         
	                         (img/concat-imgs
	                           (map #(fun.imagej.ops.morphology/dilate %1
	                                                                   (shape/sphere-shape 3))
	                                ;(img/dimension-split (img/copy initial) 2)
	                                (img/dimension-split (img/copy initial) 2))))))
	(println "Constructed positive target")
	(ij/save-img positive-target "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004_positiveTarget.tif")
	(ij/show positive-target "Positive target")
	
	#_(def target (fun.imagej.ops.threshold/intermodes 
	               (fun.imagej.ops.convert/float32
	                           (ij/open-img "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004_positiveMask.tif"))))
	  
	(def seg (atom (fseg/create-segmentation-meta {:num-positive-samples 2000
	                                               :num-negative-samples 2000
	                                               :basename "TileScan_004"
	                                               :verbose false
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
               (fseg/add-feature-map-fn "gauss-7" (fn [target]     
	                                                   (let [output (img/create-img-like target)]
	                                                     (img/concat-imgs
	                                                       (map #(fun.imagej.ops.filter/gauss %1
                                                                                            %2
                                                                                            (double-array [7 7 0]))
	                                                            (img/dimension-split (img/copy output) 2)
	                                                            (img/dimension-split (img/copy target) 2))))))
	              (fseg/add-feature-map-fn "median-3" (fn [target]     
	                                                    (let [output (img/create-img-like target)]
	                                                      (img/concat-imgs
	                                                        (map #(fun.imagej.ops.filter/median %1
	                                                                                            %2
	                                                                                            (shape/sphere-shape 3))
	                                                             (img/dimension-split (img/copy output) 2)
	                                                             (img/dimension-split (img/copy target) 2))))))
	              (fseg/add-feature-map-fn "variance-3" (fn [target]     
	                                                      (let [output (img/create-img-like target)]
	                                                        (img/concat-imgs
	                                                          (map #(fun.imagej.ops.filter/variance %1
	                                                                                                %2
	                                                                                                (shape/sphere-shape 3))
	                                                               (img/dimension-split (img/copy output) 2)
	                                                               (img/dimension-split (img/copy target) 2))))))
	              (fseg/add-feature-map-fn "gradient-x" (fn [target]     
	                                                      (img/concat-imgs
	                                                        (map #(img/gradient (fun.imagej.ops.copy/iterableInterval %) 0)
	                                                             (img/dimension-split (img/copy target) 2)))))
	              (fseg/add-feature-map-fn "gradient-y" (fn [target]     
	                                                      (img/concat-imgs
	                                                        (map #(img/gradient (fun.imagej.ops.copy/iterableInterval %) 1)
	                                                             (img/dimension-split (img/copy target) 2)))))
               (fseg/add-feature-map-fn "gauss5-gradient-x" (fn [target]     
                                                              (let [output (img/create-img-like target)]
                                                                (img/concat-imgs
	                                                                 (map #(img/gradient 
                                                                          (fun.imagej.ops.filter/gauss %1
                                                                                                       %2
                                                                                                       (double-array [5 5 0]))
                                                                          0)
	                                                                      (img/dimension-split (img/copy output) 2)
	                                                                      (img/dimension-split (img/copy target) 2))))))
               (fseg/add-feature-map-fn "gauss5-gradient-y" (fn [target]     
                                                              (let [output (img/create-img-like target)]
                                                                (img/concat-imgs
	                                                                 (map #(img/gradient 
                                                                          (fun.imagej.ops.filter/gauss %1
                                                                                                       %2
                                                                                                       (double-array [5 5 0]))
                                                                          1)
	                                                                      (img/dimension-split (img/copy output) 2)
	                                                                      (img/dimension-split (img/copy target) 2))))))
               (fseg/add-feature-map-fn "nth-eigen-0" (fn [target]     
                                                        (nth-eigen target 0)))
               (fseg/add-feature-map-fn "nth-eigen-1" (fn [target]     
                                                        (nth-eigen target 1)))
               (fseg/add-feature-map-fn "nth-eigen-2" (fn [target]     
                                                        (nth-eigen target 2)))
	              #_(fseg/add-feature-map-fn "gradient-x" (fn [target] 
	                                                       (img/gradient target 0)))
	              #_(fseg/add-feature-map-fn "gradient-y" (fn [target] 
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
	;(reset! seg (fseg/generate-sample-points @seg (fun.imagej.ops.convert/uint8 target)))
	(reset! seg (fseg/generate-sample-points-negative-label @seg
	                                                        (fun.imagej.ops.convert/uint8 positive-target)
	                                                        (fun.imagej.ops.convert/uint8 negative-target)))
	
	(println "Generating dataset.")
	(reset! seg (fseg/generate-dataset @seg input))
	
	(println "Solving segmentation")
	(reset! seg (fseg/solve-segmentation @seg))
	      
	(println "Writing segmentation map")
	(let [segmentation (fseg/segment-image @seg input)]
	  (ij/show segmentation "Segmentation")
	  ;(ij/show positive-target "Target")
	  (ij/save-img segmentation
	               "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004_fsegMap.tif")))


