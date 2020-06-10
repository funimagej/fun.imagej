(ns fun.imagej.experimental.vnc
  (:require [fun.imagej.core :as ij]
            [fun.imagej.img :as img])
  (:import (org.janelia.saalfeldlab.n5 N5FSReader N5FSWriter DatasetAttributes GzipCompression)
           (org.janelia.saalfeldlab.n5.imglib2 N5Utils N5CellLoader)
           (net.imglib2.img.array ArrayImgs)
           (net.imglib2.cache.img SingleCellArrayImg$CellArrayCursor SingleCellArrayImg LoadedCellCacheLoader DirtyDiskCellCache AccessIo CachedCellImg DiskCellCache)
           (net.imglib2.type.numeric.integer UnsignedByteType)
           (net.imglib2.img.basictypeaccess AccessFlags)
           (net.imglib2.img.cell CellGrid)
           (net.imglib2.cache IoSync)
           (net.imglib2.cache.ref GuardedStrongRefLoaderRemoverCache)
           (net.imglib2.img.basictypeaccess.array DirtyByteArray)
           (net.imglib2.type PrimitiveType)
           (com.kephale.vnc DagmarCost Utils CostUtils)
           (net.imglib2.util Intervals Util)
           (net.imglib2.view Views)
           (net.imglib2 FinalInterval)
           (net.preibisch.surface Test)))

(defn block-size
  "Return the block size of a dataset in a n5"
  [n5 dataset]
  (.getAttribute
    n5
    dataset
    "blockSize"
    (class (long-array 0))))

(defn dimensions
  "Return the dimensions of a dataset in a n5"
  [n5 dataset]
  (.getAttribute
    n5
    dataset
    "dimensions"
    (class (long-array 0))))

#_(defn block-offsets
    "Return all offsets of 1 interval with respect to another. Doesnt use imglib2 structures tho, or max/min"
    ([interval block-size]
     (block-offsets interval block-size 0))
    ([interval block-size d]
     (println d)
     (vec
       (if (= d (dec (count interval)))
         (range 0 (nth interval d) (nth block-size d))
         (let [sub-offsets (block-offsets interval
                                          block-size
                                          (inc d))]
           (println :sub-offsets sub-offsets)
           (map (fn [do]
                  ;(println do)
                  (map #(conj %
                              do)
                       sub-offsets))
                (range 0 (nth interval d) (nth block-size d))))))))

(defn read-block
  "Read a block from a n5 dataset"
  [n5 dataset dataset-attributes grid-position]
  (.getData
    (.readBlock n5
                dataset
                dataset-attributes
                grid-position)))

(defn rai-block-by-index
  "Return a rai interval of block size for block index idx"
  [cost-column block-size idx]
  (Views/interval cost-column
                  (FinalInterval. (long-array [0
                                               (* idx (second block-size))
                                               0])
                                  (long-array [(dec (first block-size))
                                               (dec (* (inc idx)
                                                       (second block-size)))
                                               (dec (last block-size))]))))

(defn demo
  "An example demo"
  []
  (let [n5-path "/Users/kharrington/Dropbox/portableVNC.n5"
        dataset "/zcorr/Sec19___20200203_085722/s0"
        cost-dataset "/cost/Sec19___20200203_085722/s0"
        heightfield-dataset "/heightfields/Sec19___20200203_085722/s0"
        n5 (N5FSReader. n5-path)
        n5-writer (N5FSWriter. n5-path)
        block-size (seq (block-size n5 dataset))
        dimensions (seq (dimensions n5 dataset))
        cost-dimension 1
        ; bc == block column
        first-start-time (System/currentTimeMillis)
        bc-dimension (map #(Math/ceil (/ %1 %2)) dimensions block-size)]

    (println :block-size block-size
             :dimensions dimensions
             :bc-dimension bc-dimension)

    ;; Work on one test column
    (let [xo 0
          zo 0
          bc-offsets (map #(vector xo % zo)
                          (range 0 (Math/ceil
                                     (/ (second dimensions)
                                        (second block-size)))))
          grid-positions (map long-array bc-offsets)
          dataset-attributes (.getDatasetAttributes n5 dataset)

          cell-loader (N5CellLoader. n5 dataset (int-array block-size))

          ; For whole image
          full-grid (CellGrid. (long-array dimensions)
                               (int-array block-size))
          ; For test column
          grid (CellGrid. (long-array (concat (take cost-dimension block-size)
                                              [(nth dimensions cost-dimension)]
                                              (drop (inc cost-dimension) block-size)))
                          (int-array block-size))
          type (UnsignedByteType.)
          entities-per-pixel (.getEntitiesPerPixel type)
          cache-loader (LoadedCellCacheLoader/get grid cell-loader type (AccessFlags/setOf AccessFlags/DIRTY))
          block-cache (DiskCellCache/createTempDirectory "CellImg" true)
          disk-cache (DirtyDiskCellCache. block-cache
                                          grid
                                          cache-loader
                                          (AccessIo/get PrimitiveType/SHORT (AccessFlags/setOf AccessFlags/DIRTY))
                                          entities-per-pixel)
          iosync (IoSync. disk-cache)
          cache (-> (GuardedStrongRefLoaderRemoverCache. 100)
                    (.withRemover iosync)
                    (.withLoader iosync))

          column (CachedCellImg. grid type cache (DirtyByteArray. 0))

          ops (.op (ij/get-ij))
          log (.log (ij/get-ij))

          ;x 0
          ;column-slice (img/hyperslice column 2 x)
          ;_ (println :adsf)
          ;cost-slice (DagmarCost/computeResin column-slice ops 1 log)

          cost-column (img/concat-imgs
                        (map (fn [x]
                               (println :step x :of (nth block-size 2))
                               (let [column-slice (img/hyperslice column 2 x)]
                                 (DagmarCost/computeResin column-slice ops 1 log)))
                             (range (nth block-size 2))))
          _ (def pre-global-cost-column cost-column)
          _ (println cost-column)
          cost-column (CostUtils/initializeCost (CostUtils/doubleAsUnsignedByte cost-column))]

      ; now everything is loaded to compute the corresponding cost volume

      ; process slicewise w.r.t. cost-dimension

      ;(ij/show column-slice)
      ;(ij/show cost-slice)

      (ij/show column)
      (ij/show cost-column)
      ;(ij/show (img/hyperslice column 2 0))

      ; TODO: Create something bigger than the column (inpaint for boundaries), if the gauss is to be handled properly

      ; now write cost

      (def global-cost cost-column)

      (when-not (.exists n5 cost-dataset)
        (let [attributes (DatasetAttributes. (Intervals/dimensionsAsLongArray cost-column)
                                             (int-array block-size)
                                             (N5Utils/dataType (Util/getTypeFromInterval column))
                                             (GzipCompression.))]
          (.createDataset n5-writer cost-dataset attributes)))

      (do
        (def dimensions dimensions)
        (def block-size block-size)
        (def cost-column pre-global-cost-column)
        (def n5-writer n5-writer)
        (def cost-dataset cost-dataset)
        (def heightfield-dataset heightfield-dataset))

      (doseq [idx (range 0 (Math/ceil (/ (nth dimensions 1) (nth block-size 1))))]
        (println :cost-column cost-column)
        (let [block (rai-block-by-index (CostUtils/floatAsUnsignedByte cost-column) block-size idx)
              grid-offset (long-array [0 idx 0])]
          (println :block block)
          (println :save-block idx (Math/ceil (/ (nth dimensions 1) (nth block-size 1))))
          (N5Utils/saveBlock block
                             n5-writer
                             cost-dataset
                             grid-offset)))

      ;; Now compute and save the heightfields
      (let [cost-interval (Views/zeroMin (Views/interval cost-column
                                                         #_(FinalInterval. (long-array [0 2700 0])
                                                                           (long-array [24 2800 24]))
                                                         (FinalInterval. (long-array [0 2500 0])
                                                                         (long-array [127 2900 127]))))
            start-time (System/currentTimeMillis)
            heightmap (Test/process2 (Views/permute cost-interval 1 2)
                                     1
                                     15
                                     3)]
        (println "Heightfield took: " (- (System/currentTimeMillis) start-time) "ms")
        (ij/show heightmap)

        (def heightmap heightmap)

        (when-not (.exists n5-writer heightfield-dataset)
          (let [attributes (DatasetAttributes. (long-array [(first dimensions)
                                                            (last dimensions)])
                                               (int-array [(first block-size)
                                                           (last block-size)])
                                               (N5Utils/dataType (Util/getTypeFromInterval heightmap))
                                               (GzipCompression.))]
            (.createDataset n5-writer heightfield-dataset attributes)))

        (let [grid-offset (long-array [0 0])]
          (N5Utils/saveBlock heightmap
                             n5-writer
                             heightfield-dataset
                             grid-offset))
        (println "Total time of whole block column calculation: " (- (System/currentTimeMillis) first-start-time))))))



(defn -main
  [& args]
  (demo))

(-main)