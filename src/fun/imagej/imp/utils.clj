(ns fun.imagej.imp.utils
  (:require [fun.imagej.imp :as imp])
  (:import [ij IJ ImagePlus ImageStack]))

(defn set-foreground
  "Set the ImageJ1 foreground color. Use 0-255 r g b colors"
  [r g b]
  (ij.IJ/setForegroundColor r g b))
(def set-foreground! set-foreground)

(defn set-background
  "Set the ImageJ1 background color. Use 0-255 r g b colors"
  [r g b]
  (ij.IJ/setBackgroundColor r g b))
(def set-background! set-background)

