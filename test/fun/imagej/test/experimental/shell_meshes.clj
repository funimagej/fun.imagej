(ns fun.imagej.test.experimental.shell-meshes
  (:require [fun.imagej.img :as img]
            [fun.imagej.conversion :as convert]
            [fun.imagej.core :as ij]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.img.cursor :as cursor]
            [clojure.string :as string]
            [random-forests.core :as rf]
            [fun.imagej.ops :as ops]
            [clojure.test :refer :all]
            [fun.imagej.mesh :as msh]
            [fun.imagej.sciview :as sv])
  (:import (net.imagej.mesh DefaultMesh)
           (net.imglib2.type.logic BitType)
           (net.imglib2 FinalInterval Cursor IterableInterval)))

;; TODO:
; - convert mesh into imagej-ops version so voxelization works. hide this from the user in 1 more function layer

;(println (.size (.getTriangles my-mesh)))

(defn convert-ijmesh-to-opsmesh
  "Convert an imagej-mesh to an imagej-ops mesh"
  [ijm]
  (let [ijo (net.imagej.ops.geom.geom3d.mesh.DefaultMesh.)]
    (doseq [tri (.getTriangles ijm)]
      (let [mv0 (.getVertex tri 0)
            mv1 (.getVertex tri 1)
            mv2 (.getVertex tri 2)
            face (net.imagej.ops.geom.geom3d.mesh.TriangularFacet. (net.imagej.ops.geom.geom3d.mesh.Vertex. (.getX mv0) (.getY mv0) (.getZ mv0))
                                                                   (net.imagej.ops.geom.geom3d.mesh.Vertex. (.getX mv1) (.getY mv1) (.getZ mv1))
                                                                   (net.imagej.ops.geom.geom3d.mesh.Vertex. (.getX mv2) (.getY mv2) (.getZ mv2)))]
        (.addFace ijo face)))
    ijo))

(defn voxelization
  "Run voxelization on a mesh"
  [m w h d]
  (.voxelization (.geom (.op ij/ij)) m w h d))

(defn scale-triangle!
  "Scale a triangle by a bounding box and dimension factors.
  Resulting verts will be inside interval [0,0,0] -> [w,h,d]"
  [tri bb w h d]
  (dotimes [k 3]; 3 vertices
    (dotimes [j 3]; 3 dimensions
      (let [val (/ (- (.getFloatPosition (.getVertex tri k) j)
                      (.realMin bb j))
                   (- (.realMax bb j) (.realMin bb j)))]
        #_(println val (.getFloatPosition (.getVertex tri k) j) (.realMin bb j) (.realMax bb j))
        (cond (= j 0) (.setX (.getVertex tri k)
                             (* w val))
              (= j 1) (.setY (.getVertex tri k)
                             (* h val))
              (= j 2) (.setZ (.getVertex tri k)
                             (* d val))))))
  tri)

;(count (.getVertices my-mesh))

(defn scale-mesh!
  "Scale a mesh to w h d dimensions"
  [m w h d]
  (let [bb (msh/bounding-interval (.getVertices m))]
    (println "bounding box:" (doall (for [j (range 3)] [(.realMin bb j) (.realMax bb j)])))
    (doall
     (for [tri (seq (.getTriangles m))]
       (do
       ;(println "O:" tri)
       (let [scaled-tri (scale-triangle! tri bb w h d)]
;         (println "S:" scaled-tri)
         scaled-tri)))))
  m)

;(def tmp-scaled (scale-mesh! my-mesh 100 100 100))
;(println (first (.getTriangles tmp-scaled)))


(defn tri-box-overlap
  "Test if a triangle overlaps with a box"
  [box-corner box-dim tri]
  true
  )

(defn voxelization2
  "Let's do our own voxelization"
  [m w h d]
  (let [out (fun.imagej.ops.create/img (net.imglib2.FinalInterval. (long-array [w h d])) (BitType.))
        out-ra (.randomAccess out)]
    (doseq [tri (.getTriangles m)]
      (let [verts (map #(.getVertex tri %) (range 3))
            min-tri-boundary (map (fn [d]
                                    (apply min (map #(.getFloatPosition % d) verts)))
                                  (range 3))
            max-tri-boundary (map (fn [d]
                                    (apply max (map #(.getFloatPosition % d) verts)))
                                  (range 3))
            inds (long-array 3)]
        (doall
          (for [x (range (first min-tri-boundary) (first max-tri-boundary))
                y (range (second min-tri-boundary) (second max-tri-boundary))
                z (range (last min-tri-boundary) (last max-tri-boundary))]
            (when-not (.get (.get out-ra))
              (when (tri-box-overlap [x y z] [1 1 1] tri)
                (.set (.get out-ra) true)))))))
    out))

(defn voxelization3
  "Let's do our own voxelization.
  Use imglib2 Polygon's rasterization"
  [m w h d]
  (let [vxls (atom (fun.imagej.ops.create/img (long-array [w h d])))]
    (loop [tris (iterator-seq (.getTriangles m))]
      (when-not (empty? tris)
        (let [tri (first tris)
              tri-raster (.rasterize (net.imglib2.roi.geometric.Polygon. (.getVertices tri)))]; check alignment of both intervals
          ;(swap! vxls #(fun.imagej.ops.logic/and % tri-raster))
          (swap! vxls #(first
                         (img/map-img! (fn [c1 c2]
                                         (println "c1 " (cursor/get-val c1) " c2 " (cursor/get-val c2))
                                         (when (cursor/get-val c2)
                                           (cursor/set-val c1 c2)))
                                       % tri-raster)))
          (recur (rest tris)))))
    @vxls))


;(println (first (.getTriangles scaled-mesh)))

;(def voxelized (voxelization3 scaled-mesh 100 100 100))
;(ij/show voxelized)

(defn map-min-rai
  "Map over the minimum RAI for each bound. f is a cursor function"
  [f img1 img2]
  (let [isx (net.imglib2.util.Intervals/intersect img1 img2)
        v1 (net.imglib2.view.Views/interval img1 isx)
        v2 (net.imglib2.view.Views/interval img2 isx)

        ^Cursor cur1 (.cursor ^IterableInterval v1)
        ^Cursor cur2 (.cursor ^IterableInterval v2)
        t (Thread.
            (fn []
              (loop []
                (when (and (.hasNext cur1)
                           (.hasNext cur2))
                  (.fwd cur1)
                  (.fwd cur2)
                  (f cur1 cur2)
                  (recur)))))]
    (.start t)
    (.join t)
    [img1 img2]))

(do
  (def filename "/Users/kharrington/Data/WieseRobert/Cip1.stl")

  (def my-mesh (msh/read-stl filename))
  (println (first (.getTriangles my-mesh)))

  (let [bounding-coords (msh/real-interval-to-seq (msh/bounding-interval (.getVertices my-mesh)))
        min-coords (take 3 bounding-coords)]
    (println :min-coords min-coords)
    (doseq [^net.imagej.mesh.Vertex3 v (.getVertices my-mesh)]
      (.setX v (- (.getX v) (first min-coords)))
      (.setY v (- (.getY v) (second min-coords)))
      (.setZ v (- (.getZ v) (last min-coords)))))

  (println (take 10 (.getVertices my-mesh)))

  ;(def scaled-mesh (scale-mesh! my-mesh 100 100 100))
  ;(def scaled-mesh (scale-mesh! my-mesh 100 100 100))
  (def tris (seq (.getTriangles my-mesh)))
  (def tri (first tris))
  (.get (.getTriangles my-mesh) 1)
  ;(nth tris 0)
  ;[(first tris) (second tris)]
  (println (count tris))
  (println tri)
  (def tri-raster (.rasterize (net.imglib2.roi.geometric.Polygon. (.getVertices tri))))
  (println tri-raster (doall (map #(vector % (.realMin tri-raster %) (.realMax tri-raster %)) (range 3))))
  #_(println (seq (img/dimensions tri-raster)))
  #_(println (doall
             (for [x (range (first (seq (img/dimensions tri-raster))))
                   y (range (second (seq (img/dimensions tri-raster))))
                   z (range (last (seq (img/dimensions tri-raster))))]
               (img/get-val tri-raster (long-array [x y z])))))
  ;(def vxls3 (voxelization3 scaled-mesh 100 100 100))
  ;(defonce ui (ij/show-ui))

  ;(ij/show tri-raster)

  ;(ij/show vxls3)

  ;(def vxls (atom (fun.imagej.ops.create/img (long-array [100 100 100]))))
  #_(def vxls2 (first
               (img/map-img! (fn [c1 c2]
                               (println "c1 " (cursor/get-val c1) " c2 " (cursor/get-val c2))
                               (cursor/set-val c1
                                               (cursor/mul c1 c2)))
                             @vxls tri-raster)))
  #_(def vxls2 (first
               (map-min-rai (fn [c1 c2]
                              (let [p1 (long-array 3)
                                    p2 (long-array 3)]
                                (.localize c1 p1)
                                (.localize c2 p2)
                               (println "c1 " (.get (.get c1)) " c2 " (.get (.get c2))
                                        " p1 " (seq p1) " p2 " (seq p2))
                                        )
                              (when (.get (.get c2))
                                (.set (.get c1) (.get c2)))
                              #_(.mul (.get c1) (.get c2))
                              #_(.set (.get c1)
                                      (.mul (.get c1) (.get c2))))
                             @vxls tri-raster)))
  #_(println @vxls (doall (map #(vector % (.realMin @vxls %) (.realMax @vxls %)) (range 3))))

  #_(println "CURS" (.get (.fwd (.cursor tri-raster))) (.get (.fwd (.cursor @vxls))))

  #_(def vxls2 (first
               (img/map-img! (fn [c1]
                               (println "c1 " (cursor/get-val c1)))
                             @vxls )))

  )

;(ij/show-ui)

;(.size (.getVertices ops-mesh))

;(sv/open-obj filename)
;(sv/open-stl filename)

;(def ops-mesh (convert-ijmesh-to-opsmesh my-mesh))
;(def voxelized (voxelization2 scaled-mesh 100 100 100))
;(def voxelized (ops/run-op "geom.voxelization" [(convert-ijmesh-to-opsmesh my-mesh) 50 50 50]))
;(ij/show voxelized)
;(.voxelization (.geom (.op ij/ij)) ops-mesh)
