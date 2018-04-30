(ns fun.imagej.core
  (:require [clojure.pprint :as pprint]
            [clojure.string :as string]
            #_[fun.imagej.img :as img])
  (:import [net.imagej ImageJ]))

; ImageJ gateway
(defonce ij (atom nil))

(defn get-ij
  "Get the currently active ImageJ gateway."
  []
  (when-not @ij
    (let [context (org.scijava.Context.)
          new-ij (net.imagej.ImageJ. context)]
      (reset! ij new-ij)))
  @ij)

(defn open-img
  "Open an image with ImageJ/SCIFIO"
  [filename]
  (.getImg (.getImgPlus (.open (.datasetIO (.scifio (get-ij))) filename))))

(defn save-img
  "Open an image with ImageJ/SCIFIO"
  [img filename]
  (.save (.datasetIO (.scifio (get-ij)))
    (net.imagej.DefaultDataset. 
      (.context (get-ij)) (net.imagej.ImgPlus. img))
    filename)
  img)

(defn show
  "Show an image with ImageJ."
  ([img]
    (.show (.ui (get-ij)) img)
    img)
  ([img title]
    (.show (.ui (get-ij)) title img)
    img))

(defn notebook-show
  [image]
  (.display (.notebook (get-ij)) image)
  image)

(defn show-ui
  "Show the ImageJ UI"
  []
  (.showUI (.ui (get-ij))))

(defn setup-context
  "Setup the context."
  [ctxt source-ns]
  (refer source-ns)
  ; Could check if the existing IJ is already using the same context, because it probably is
  (reset! ij (net.imagej.ImageJ. (if ctxt ctxt (org.scijava.Context.)))))

