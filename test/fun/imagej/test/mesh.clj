(ns fun.imagej.test.mesh
  (:use [fun.imagej imp conversion]        
        [clojure.test])
  (:require [fun.imagej.mesh :as msh]
            [clojure.java.io :as io]))

(deftest test-read-stl
  (let [m (msh/read-stl (io/resource "stl/sphere.stl"))]
    #_(is (= (count (.getVertices m))
             242)); This is what the result would be if we avoided duplicate vertices
    (is (= (count (.getVertices m))
           1440))))

(deftest test-vertex-mutation
    (let [m (msh/read-stl (io/resource "stl/sphere.stl"))
          copied-verts (doall; This doall is important
                         (map msh/vertex3-to-array
                              (.getVertices m)))]
      (doseq [v (.getVertices m)]
        (.setX v (+ (.getX v) 100))
        (.setY v (+ (.getY v) 100))
        (.setZ v (+ (.getZ v) 100)))
      (is (not (zero?
                 (reduce +
                         (map -
                              (first copied-verts)
                              (msh/vertex3-to-array (first (.getVertices m))))))))))
