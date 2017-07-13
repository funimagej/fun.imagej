(ns fun.imagej.img.shape
  (:import [net.imglib2.algorithm.neighborhood Neighborhood RectangleShape]))

(defn rectangle-shape
  "Return a RectangleShape."
  ([length]
   (rectangle-shape length true))
  ([length exclude-center?]
    (net.imglib2.algorithm.neighborhood.RectangleShape. length exclude-center?)))
(def rectangle rectangle-shape)

(defn sphere-shape
  "Return a sphere shape"
  [radius]
  (net.imglib2.algorithm.neighborhood.HyperSphereShape. radius))
(def sphere sphere-shape)

(defn diamond
  "Return a diamond shape"
  [radius]
  (net.imglib2.algorithm.neighborhood.DiamondShape. radius))

(defn diamond-tips
  "Return a diamond-tips shape"
  [radius]
  (net.imglib2.algorithm.neighborhood.DiamondTipsShape. radius))

(defn horizontal-line
  "Return a horizontal line that spans 2xradius + 1 in a given dimension"
  [radius dimension exclude-center?]
  (net.imglib2.algorithm.neighborhood.HorizontalLineShape. radius dimension exclude-center?))

; Pair of points PairOfPointsShape
; Periodic line PeriodicLineShape
