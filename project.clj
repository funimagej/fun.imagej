(defproject fun.imagej/fun.imagej "0.4.1-SNAPSHOT"
  :description "Functional Image Processing with ImageJ/FIJI"
  :url "https://github.com/funimagej/fun.imagel"
  :license {:name "Apache v2.0"
            :url "https://github.com/funimagej/fun.imagej/LICENSE"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [seesaw "1.4.4"]
                 [clj-random "0.1.8"]

                 [random-forests-clj "0.2.0"]

                 ; Java libs
                 [net.imglib2/imglib2-algorithm "0.11.0"]
                 [net.imglib2/imglib2-roi "0.6.1"]
                 [net.imglib2/imglib2-ij "2.0.0-beta-43"]
                 [net.imagej/imagej "2.0.0-rc-68" :exclusions [com.github.jnr/jffi
                                                               com.github.jnr/jnr-x86asm
                                                               org.scijava/scripting-renjin]]
                 [ome/bioformats_package "6.0.0-m2"]
                 [net.imagej/imagej-ops "0.43.0"]
                 [net.imagej/imagej-mesh "0.7.1"]
                 [net.imagej/imagej-mesh-io "0.1.2"]

                 [org.janelia.saalfeldlab/n5 "2.0.2"]
                 [org.janelia.saalfeldlab/n5-imglib2 "3.0.0"]

                 [org.ojalgo/ojalgo "46.3.0"]

                 [sc.fiji/Auto_Threshold "1.17.1"]

                 [org.scijava/scijava-common "2.76.0"]

                 [sc.iview/sciview "d8a6df9" :exclusions [com.github.jnr/jffi
                                                          com.github.jnr/jnr-x86asm
                                                          org.scijava/scripting-renjin]]]
  :resource-paths ["src/main/resource"]
  :java-source-paths ["java"]
  :repositories [["scijava.public" "https://maven.scijava.org/content/groups/public"]
                 ["jitpack.io" "https://jitpack.io"]]
  :deploy-repositories [["releases" {:url "https://maven.imagej.net/content/repositories/releases"
                                     ;; Select a GPG private key to use for
                                     ;; signing. (See "How to specify a user
                                     ;; ID" in GPG's manual.) GPG will
                                     ;; otherwise pick the first private key
                                     ;; it finds in your keyring.
                                     ;; Currently only works in :deploy-repositories
                                     ;; or as a top-level (global) setting.
                                     :username :env/CI_DEPLOY_USERNAME
                                     :password :env/CI_DEPLOY_PASSWORD
                                     :sign-releases false}]
                        ["snapshots" {:url "https://maven.imagej.net/content/repositories/snapshots"
                                      :username :env/CI_DEPLOY_USERNAME
                                      :password :env/CI_DEPLOY_PASSWORD
                                      :sign-releases false}]]
  ; Try to use lein parent when we can
  :plugins [[lein-exec "0.3.7"]]
  :jvm-opts ["-Xmx32g" "-server" "-Dscenery.Renderer=OpenGLRenderer"
             ;"-javaagent:/Users/kyle/.m2/repository/net/imagej/ij1-patcher/0.12.3/ij1-patcher-0.12.3.jar=init"
             #_"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:8000"])
