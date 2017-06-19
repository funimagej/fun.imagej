(ns fun.imagej.img.shape
  (:import [net.imglib2.algorithm.neighborhood Neighborhood RectangleShape]))

(defn rectangle-shape
  "Return a RectangleShape."
  [length]
  (net.imglib2.algorithm.neighborhood.RectangleShape. length true))

(defn sphere-shape
  "Return a sphere shape"
  [radius]
  (net.imglib2.algorithm.neighborhood.HyperSphereShape. radius))
