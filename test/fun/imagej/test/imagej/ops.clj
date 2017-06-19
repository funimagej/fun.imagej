(ns fun.imagej.test.imagej.ops
  (:use [fun.imagej imp conversion]        
        [clojure.test])
  (:require [fun.imagej.img :as img]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.img.utils :as img-utils]))

(deftest test-ops
  (let [img (fun.imagej.ops.create/img (img-utils/interval [10 10]) (net.imglib2.type.numeric.real.DoubleType.))]    
    (is img)))

#_(do 
   (ij/show-ui)
   (let [cat (ij/open-img "/Users/kharrington/git/funimage/black_cat.tif")
         hat (ij/open-img "/Users/kharrington/git/funimage/witch_hat_small.tif")
         adder (fn [img1 img2] (img/replace-subimg-with-opacity img1 img2 [110 50] 0))]
     (ij/show (img-utils/tile-imgs
                (map (fn [func] (adder (img/copy cat) (func (img/copy hat))))
                     (mapcat (fn [r]
                               [#(funimage.imagej.ops.morphology/dilate % (shape/rectangle-shape r))
                                #(funimage.imagej.ops.morphology/erode % (shape/rectangle-shape r))])
                             (range 3 7)))))))
