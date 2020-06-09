(ns fun.imagej.experimental.vnc
  (:require [fun.imagej.core :as ij]
            [fun.imagej.img :as img])
  (:import (org.janelia.saalfeldlab.n5 N5FSReader)
           (org.janelia.saalfeldlab.n5.imglib2 N5Utils N5CellLoader)
           (net.imglib2.img.array ArrayImgs)
           (net.imglib2.cache.img SingleCellArrayImg$CellArrayCursor SingleCellArrayImg LoadedCellCacheLoader DirtyDiskCellCache AccessIo CachedCellImg DiskCellCache)
           (net.imglib2.type.numeric.integer UnsignedByteType)
           (net.imglib2.img.basictypeaccess AccessFlags)
           (net.imglib2.img.cell CellGrid)
           (net.imglib2.cache IoSync)
           (net.imglib2.cache.ref GuardedStrongRefLoaderRemoverCache)
           (net.imglib2.img.basictypeaccess.array DirtyByteArray)
           (net.imglib2.type PrimitiveType)))

(def n5-path "/Users/kharrington/Dropbox/portableVNC.n5")
(def dataset "/zcorr/Sec19___20200203_085722/s0")

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

(let [n5 (N5FSReader. n5-path)
      block-size (seq (block-size n5 dataset))
      dimensions (seq (dimensions n5 dataset))
      cost-dimension 1
      ; bc == block column
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

        column (CachedCellImg. grid type cache (DirtyByteArray. 0))]

    ;(ij/show column)

    (ij/show (img/hyperslice column 2 0))

    ; random forest to classify surface voxels?

    ;; fill column
    (println column)))


  ; The cost algorithm works in columns along a specified dimension (cost-dimension).
  ; So let's iterate over columns of blocks w.r.t. the same dimension.

  ;(let [block-columns]))


; Could we even flatten w.r.t these columns of blocks?

