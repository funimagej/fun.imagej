(ns fun.imagej.test.img
  (:use [fun.imagej imp conversion]        
        [clojure.test])
  (:require [fun.imagej.img :as img]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.img.type :as imtype]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]))

(deftest test-img-add
  (let [w 5 h 5
        img1 (first (img/map-img cursor/set-one
                               (imp->img (create-imp :width w :height h))))
        img2 (first (img/map-img cursor/set-one
                               (imp->img (create-imp :width w :height h))))
        img-res (first (img/map-img cursor/add
                                  img1 img2))
        img-sum (img/sum img-res)]
    (is (= img-sum (* 2 w h)))))

(deftest test-img-inc
  (let [w 5 h 5
        img1 (first (img/map-img cursor/inc
                             (imp->img (create-imp :width w :height h))))
        img-sum (img/sum img1)]
    (is (= img-sum (* w h)))))

(deftest test-get-val
  (let [w 5 h 5
        img1 (first (img/map-img cursor/inc
                                 (imp->img (create-imp :width w :height h))))]
    (is (= 1 (imtype/get-type-val (.firstElement img1))))))
    
(deftest test-replace-subimg
  (let [img1 (fun.imagej.ops.create/img (net.imglib2.FinalInterval. (long-array [100 100]))
                                             (net.imglib2.type.numeric.real.DoubleType.))
        img2 (fun.imagej.ops.create/img (net.imglib2.FinalInterval. (long-array [50 50]))
                                             (net.imglib2.type.numeric.real.DoubleType.))]
    (img/map-img cursor/set-zero img1); Fill img1
    (img/map-img cursor/set-one img2); Fill img2
    (img/replace-subimg img1 img2 [25 25])
    (let [ra (.randomAccess img1)]
      (.setPosition ra (long-array [50 50]))
      (is (zero? (- (.get (.get ra)) 1))))))
             
         
        
