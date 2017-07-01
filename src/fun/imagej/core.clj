(ns fun.imagej.core
  (:require [clojure.pprint :as pprint]
            [clojure.string :as string]
            #_[fun.imagej.img :as img])
  (:import [net.imagej ImageJ]))

(let [context (org.scijava.Context.) 
      new-ij (net.imagej.ImageJ. context)]
  (defonce ij new-ij))

(defn open-img
  "Open an image with ImageJ/SCIFIO"
  [filename]
  (.getImg (.getImgPlus (.open (.datasetIO (.scifio ij)) filename))))

(defn save-img
  "Open an image with ImageJ/SCIFIO"
  [img filename]
  (.save (.datasetIO (.scifio ij))
    (net.imagej.DefaultDataset. 
      (.context ij) (net.imagej.ImgPlus. img)) 
    filename)
  img)

(defn show
  "Show an image with ImageJ."
  ([img]
    (.show (.ui ij) img)
    img)
  ([img title]
    (.show (.ui ij) title img)
    img))

(defn notebook-show
  [image]
  (.display (.notebook ij) image)
  image)

(defn show-ui
  "Show the ImageJ UI"
  []
  (.showUI (.ui ij)))
