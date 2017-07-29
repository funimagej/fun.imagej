(ns plugins.Scripts.Plugins.fun.imagej.Experimental.osteoblast-segmentation-003
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

(defn normalize
  "Normalize an interable interval"
  [input]
  (let [mx (.get (fun.imagej.ops.stats/max input))
        mn (.get (fun.imagej.ops.stats/min input))
        denom (- mx mn)]
    (first (img/map-img #(cursor/set-val % (/ (- (cursor/get-val %) mn ) denom))
                        input))))

(defn invert
  "Invert an image"
  [input]
  (fun.imagej.ops.image/invert (fun.imagej.ops.convert/float32
                                 (fun.imagej.ops.create/img (img/dimensions input)))
                               input))

(defn -main
  [& args]
  (ij/show-ui)
  (def input (normalize (fun.imagej.ops.convert/float32 (ij/open-img "/Users/kharrington/Data/Tabler_Jacqui/15MAY_osx_1.lif - TileScan_004.tif"))))
  (def borders (normalize (fun.imagej.ops.convert/float32 (cell-borders input))))

  (ij/show input "Input")
  (ij/show borders "Borders")

  #_(def separated-cells (fun.imagej.ops.math/subtract input
                                                       (fun.imagej.ops.math/multiply borders 2)))

  #_(def separated-cells (invert (fun.imagej.ops.math/multiply input
                                                            (invert borders))))

  #_(def separated-cells (invert (fun.imagej.ops.math/multiply input
                                                             (fun.imagej.ops.filter/gauss
                                                               (fun.imagej.ops.create/img borders)
                                                               (invert borders)
                                                               (double-array [5 5 0])))))

  ;(ij/show (fun.imagej.ops.math/subtract input borders))
  (def separated-cells (fun.imagej.ops.math/multiply input
                                                    (fun.imagej.ops.math/subtract input borders)))

  #_(ij/show (smooth (fun.imagej.ops.math/multiply input
                                                 (fun.imagej.ops.math/subtract input borders))))

  (ij/show separated-cells "Separated")

  (ij/show (smooth separated-cells) "smooth sep")

  ;(ij/show (smooth (img/copy borders)))
  )

(-main)

