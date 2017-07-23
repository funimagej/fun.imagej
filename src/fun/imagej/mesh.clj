(ns fun.imagej.mesh
  (:import [net.imagej.mesh.stl STLFacet BinarySTLFormat])
  (:require [fun.imagej.img :as img]
            [fun.imagej.imp :as imp]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.conversion :as iconv]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn vertex-to-vector3d
  "Convert a Vertex to Vector3D."
  [vtx]
  (org.apache.commons.math3.geometry.euclidean.threed.Vector3D.
    (.getX vtx)
    (.getY vtx)
    (.getZ vtx)))

(defn marching-cubes
  "Convenience function for marching cubes."
  [input]
  (fun.imagej.ops.geom/marchingCubes (fun.imagej.ops.convert/bit input)))

(defn write-mesh-as-stl
  "Write a DefaultMesh from imagej-ops to a .stl file."
  [mesh stl-filename]
  (let [default-mesh (net.imagej.mesh.DefaultMesh.)
        stl-facets (for [facet (.getFacets mesh)]
                     (let [normal (.init (.create (.getVertex3Pool default-mesh))
                                         (.getX (.getNormal facet)) (.getY (.getNormal facet)) (.getZ (.getNormal facet)))
                           v1 (.init (.create (.getVertex3Pool default-mesh))
                                     (.getX (.getP0 facet)) (.getY (.getP0 facet)) (.getZ (.getP0 facet)))
                           v2 (.init (.create (.getVertex3Pool default-mesh))
                                     (.getX (.getP1 facet)) (.getY (.getP1 facet)) (.getZ (.getP1 facet)))
                           v3 (.init (.create (.getVertex3Pool default-mesh))
                                     (.getX (.getP2 facet)) (.getY (.getP2 facet)) (.getZ (.getP2 facet)))]
                       (.init (.create (.getTrianglePool default-mesh))
                              v1 v2 v3 normal)))
        ofile (java.io.FileOutputStream. stl-filename)]
    (.write ofile
      (.write (BinarySTLFormat.)
        stl-facets))
    (.close ofile)))

#_(defn read-stl-mesh; This shouldn't be called a mesh function because it only returns vertices
   "Read a mesh from a STL file."
   [stl-filename]
   (let [facets (.read (BinarySTLFormat.) (io/file stl-filename))]
     (for [facet facets
           vertex [(.vertex0 facet) (.vertex1 facet) (.vertex2 facet)]]
       vertex)))

(defn read-stl-vertices
   "Read a mesh from a STL file."
   [stl-filename]
   (let [default-mesh (net.imagej.mesh.DefaultMesh.)
         facets (.read (BinarySTLFormat.)
                       (.getTrianglePool default-mesh)
                       (.getVertex3Pool default-mesh)
                       (io/file stl-filename))]
     (doall
       (for [^net.imagej.mesh.Triangle facet facets
             ^net.imagej.mesh.Vertex3 vertex [(.getVertex facet 0) (.getVertex facet 1) (.getVertex facet 2)]]
         (net.imglib2.RealPoint. (double-array [(.getX vertex) (.getY vertex) (.getZ vertex)]))))))

(defn read-vertices-to-xyz
  "Write a list of vertices to xyz."
  [filename]
  (let [contents (slurp filename)]
    (doall
      (for [line (string/split-lines contents)]
        (net.imglib2.RealPoint. (double-array (map #(Double/parseDouble %) (string/split line #"\t"))))))))

(defn merge-vertices-by-distance
  "Merge vertices that are close within a given distance threshold.
Expects vertices to be a sequence of RealLocalizable's.
Returns a sequence of RealLocalizable's"
  [vertices distance-threshold]
  (let [point-list ^net.imglib2.RealPointSampleList (net.imglib2.RealPointSampleList. 3)
        _ (doseq [vert vertices]
            (.add point-list ^net.imglib2.RealLocalizable vert;; Let's update this to use real localizables
                  (net.imglib2.type.logic.BitType. false)))
        kdtree (net.imglib2.KDTree. point-list)
        tree-search ^net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree (net.imglib2.neighborsearch.RadiusNeighborSearchOnKDTree. kdtree)]
    ;; First search and remove
    (loop [cur ^net.imglib2.KDTree$KDTreeCursor (.cursor kdtree)]; mighe be $KDTreeCursor
      (when (.hasNext cur)
        (.fwd cur)
        (when-not (.get ^net.imglib2.type.logic.BitType (.get cur)) ;; Only look at neighbors if this point is unflagged
          (.search tree-search cur distance-threshold true)
          (when (pos? (.numNeighbors tree-search))
            (doseq [k (range 1 (.numNeighbors tree-search))]; Unsure whether input point is returned, assuming so
              (.setOne ^net.imglib2.type.logic.BitType (.get ^net.imglib2.Sampler (.getSampler tree-search k))))))
        (recur cur)))
    ;; Then return unflagged points
    (loop [cur ^net.imglib2.KDTree$KDTreeCursor (.cursor kdtree)
           result-verts []]
      (if (.hasNext cur)
        (do
          (.fwd cur)
          (if-not (.get ^net.imglib2.type.logic.BitType (.get cur)) ;; Only look at neighbors if this point is unflagged
            (recur cur (conj result-verts (net.imglib2.RealPoint. cur)))
            (recur cur result-verts)))
        result-verts))))

(defn zero-mean-vertices
  "Center a set of vertices using the mean.
Mutable function"; could be easily generalized beyond 3D
  [vertices]  
  (let [npoints (count vertices) 
        x (/ (reduce + (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 0) vertices)) npoints) 
        y (/ (reduce + (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 1) vertices)) npoints)
        z (/ (reduce + (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 2) vertices)) npoints)
        center (net.imglib2.RealPoint. (double-array [(- x) (- y) (- z)]))]
    (doseq [^net.imglib2.RealPositionable vert vertices]
      (.move vert center))
    vertices))
(def zero-mean-vertices! zero-mean-vertices)

(defn scale-vertices
  "Scale all vertices by a factor."
  [vertices scale]
  (doseq [^net.imglib2.RealPoint vert vertices]
    (dotimes [d (.numDimensions vert)]      
      (.setPosition vert (double (* scale (.getDoublePosition vert d))) (int d))))
  vertices)
(def scale-vertices! scale-vertices)

(defn bounding-interval
  "Return a RealInterval that bounds a collection of RealLocalizable vertices."
  [vertices]
  (let [min-x (reduce min (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 0) vertices)) 
        min-y (reduce min (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 1) vertices))
        min-z (reduce min (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 2) vertices))
        max-x (reduce max (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 0) vertices)) 
        max-y (reduce max (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 1) vertices))
        max-z (reduce max (map #(.getDoublePosition ^net.imglib2.RealLocalizable % 2) vertices))]
    (net.imglib2.util.Intervals/createMinMaxReal (double-array [min-x min-y min-z max-x max-y max-z]))))

(defn real-interval-to-seq
  "Convert an interval into a seq"
  [interval]
  (concat (map #(.realMin interval %) (range (.numDimensions interval)))
          (map #(.realMax interval %) (range (.numDimensions interval)))))

(defn center-vertices
  "Center the vertices using the bounding box."
  [vertices]
  (let [bb (bounding-interval vertices)
        npoints (count vertices) 
        center (net.imglib2.RealPoint. (double-array [(- (/ (- (.realMax bb 0) (.realMin bb 0)) 2))
                                                      (- (/ (- (.realMax bb 1) (.realMin bb 1)) 2)) 
                                                      (- (/ (- (.realMax bb 2) (.realMin bb 2)) 2))]))]
    (doseq [^net.imglib2.RealPositionable vert vertices]
      (.move vert center))
    vertices))
(def center-vertices! center-vertices)

#_(defn write-vertices-to-xyz
    "Write a list of vertices to xyz."
    [verts filename]
    (spit filename
          (with-out-str
            (doall
              (for [vert verts]
                (println (string/join "\t" (seq vert))))))))

(defn write-vertices-to-xyz
  "Write a list of vertices to xyz."
  [verts filename]
  (spit filename
        (with-out-str
          (doall
            (for [vert verts]
              (println (string/join "\t" [(.getDoublePosition vert 0) (.getDoublePosition vert 1) (.getDoublePosition vert 2)])))))))

(defn realpoint-to-point
  "Convert a real point to a point"
  [^net.imglib2.RealPoint rp]
  ^net.imglib2.Point (net.imglib2.Point. (long-array [(.getDoublePosition rp 0) (.getDoublePosition rp 1) (.getDoublePosition rp 2)])))


