; @File(label="Filename of mesh",description="Filename should be stl") mesh-filename
; @Integer(label="Width") width
; @Integer(label="Height") height
; @Integer(label="Depth") depth
; @Context ctxt
; @OUTPUT ImagePlus voxelized-imp

(ns plugins.Scripts.Plugins.FunImageJ.Demo.kidney-tubules
  (:require [fun.imagej.img :as img]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.conversion :as convert]
            [fun.imagej.segmentation.imp :as ij1seg]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.imp.roi :as roi]
            [fun.imagej.img.type :as imtype]
            [fun.imagej.mesh :as msh]
            [fun.imagej.imp :as ij1]
            [clojure.string :as string]
            [fun.imagej.fu.n5 :as n5]))

(defonce ui (ij/show-ui))

(def filename "/Users/kharrington/Dropbox/KidneyAnalysisCahill/LC-7-2_crop001.tif")
(def n5-cache (n5/open-n5-cache "/Users/kharrington/Dropbox/KidneyAnalysisCahill/LC-7-2_crop001/cache-test.n5"))

(def mem #(n5/memoize-with-n5-cache % n5-cache))

(def im ((mem ij/open-img) filename))
(def stroma ((mem img/hyperslice) im 2 1))

(ij/show stroma "LC-7-2_crop001")

(defn smoother
  [im r]
  (fun.imagej.ops.filter/mean (fun.imagej.ops.create/img (long-array (img/dimensions im)))
                              im
                              (shape/sphere r)))

#_(def smoother-memo (n5/memoize-with-n5-cache smoother n5-cache))

(let [start-time (System/nanoTime)]
  (def smooth-stroma ((mem smoother) stroma 5))
  (println "First time:" (float (/ (- (System/nanoTime) start-time) 1000000))))

;(ij/save-img smooth-stroma "/Users/kharrington/Dropbox/KidneyAnalysisCahill/LC-7-2_crop001_ss.tif")

(def thresh-ss ((mem fun.imagej.ops.threshold/otsu) smooth-stroma))
(ij/show thresh-ss)

#_(let [start-time (System/nanoTime)]
  (def smooth-stroma ((mem smoother) stroma))
  (println "Second time:" (float (/ (- (System/nanoTime) start-time) 1000000))))

#_(let [start-time (System/nanoTime)]
  (def smooth-stroma (smoother-memo stroma))
  (println "Third time:" (float (/ (- (System/nanoTime) start-time) 1000000))))

(ij/show smooth-stroma)
