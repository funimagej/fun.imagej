(ns fun.imagej.test.conversion
  (:use [clojure.test])
  (:require [fun.imagej.img :as img]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.conversion :as iconv]
            [clj-random.core :as rnd]))

(deftest test-imgandback
  (let [img1 (first (img/map-img (fn [^net.imglib2.Cursor cur]
                                   (cursor/set-val cur (long (rnd/lrand-int 2))))
                                 (fun.imagej.ops.create/img (net.imglib2.FinalInterval. (long-array [100 100]))
                                                            (net.imglib2.type.numeric.real.DoubleType.))))
        img2 (iconv/imp->img (iconv/img->imp img1))
        img1-sum (img/sum img1)
        img2-sum (img/sum img2)]
    (is (= img1-sum img2-sum))))


         
        
