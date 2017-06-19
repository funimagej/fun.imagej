(ns fun.imagej.img.type)

(defn double-type
  "Return an ImgLib2 DoubleType."
  []
  (net.imglib2.type.numeric.real.DoubleType.))

(defn int-type
  "Return an ImgLib2 IntType."
  []
  (net.imglib2.type.numeric.integer.IntType.))

(defn long-type
  "Return an ImgLib2 LongType."
  []
  (net.imglib2.type.numeric.integer.LongType.))

(defn byte-type
  "Return an ImgLib2 ByteType."
  []
  (net.imglib2.type.numeric.integer.ByteType.))

(defn bit-type
  "Return an ImgLib2 BitType."
  []
  (net.imglib2.type.logic.BitType.))

(defn argb-type
  "Return an ImgLib2 ARGBType."
  []
  (net.imglib2.type.numeric.ARGBType.))

(defn argb-double-type
  "Return an ImgLib2 ARGBDoubleType."
  []
  (net.imglib2.type.numeric.ARGBDoubleType.))

(defn unsigned-byte-type
  "Return an ImgLib2 UnsignedByteType."
  []
  (net.imglib2.type.numeric.integer.UnsignedByteType.))

(defn unsigned-int-type
  "Retun an ImgLib2 UnsignedIntType."
  []
  (net.imglib2.type.numeric.integer.UnsignedIntType.))

(defn unsigned-long-type
  "Return an ImgLib2 UnsignedLongType."
  []
  (net.imglib2.type.numeric.integer.UnsignedLongType.))


