(ns app.core
  (:gen-class)
  (:use markdown.core
        [clojure.string :only (join split)]
        [io.aviso.ansi]
        [io.aviso.logging])
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [cheshire.core :refer :all]))


(def default-config-file "_config.json")


(defn show-banner []
  (println "EXPERIENCE WALL"))

(defn show-help []
  (show-banner)
  (println (join "\n"
                 ["Usage:"
                  "Ewall [command]"])))

(defn read-default-config []
  (parse-string (slurp (io/resource
                        default-config-file)) true))

(defn read-project-config [])

(def default-config (read-default-config))

(def public-dir (:public_dir default-config))


(defn current-path []
  (str fs/*cwd*))

(defn join-path
  [& args]
  (join "/" args))

(defn join-path-cwd
  [path]
  (println path)
  (join-path (current-path) path))

(defn list-dirs
  [path]
  (filter fs/directory? (fs/list-dir path)))

(defn list-files
  [path]
  (filter fs/file? (fs/list-dir path)))


(defn paths-to-name
  [paths]
  (map fs/name paths))

(defn paths-to-str
  [paths]
  (map str paths))


(defn walk-source-dir
  [root dirs files]
  (println "x")
  (println (str root))
  (println (str dirs))
  (println (first files)))

(defn check-in-wall-project []
  (if-not fs/exists (join-path (current-path) default-config-file)
          (do
            (println
             (str
              (blod
               (red "Sorry, I can not found \"_config.json\" file!"))))
            (System/exit 0))))

(defn check-has-source []
  )

(defn release-wall [path]
  (check-in-wall-project)
  (let [source-sub-dirs (paths-to-str (list-dirs))])
  (fs/walk walk-source-dir path))

(release-wall "/home/tyan/DEMO/BNBB/source")


(defn new-ewall
  [rest]
  (if (< (count rest) 1)
    (do
      (fs/mkdir (join-path-cwd (:source_dir default-config)))
      (fs/mkdir (join-path-cwd (:public_dir default-config)))
      (spit (join-path-cwd default-config-file) (slurp (io/resource
                                                        default-config-file))))
    (do
      (fs/mkdir (join-path-cwd (first rest)))
      (fs/mkdir (join-path-cwd (join-path (first rest) (:source_dir default-config))))
      (fs/mkdir (join-path-cwd (join-path (first rest) (:public_dir default-config))))
      (spit (join-path-cwd (join-path (first rest) default-config-file)) (slurp (io/resource
                                       default-config-file))))))

(defn new-experience
  [rest]
  (println))

(defn start-server
  [rest]
  (println))

(defn release-experience
  [rest]
  (println))

(defn -main
  [& args]
  (if (< (count args) 1)
    (show-help)
    (let [command (first args)]
      (cond
        (= command "init") (new-ewall (rest args))
        (= command "new") (new-experience (rest args))
        (= command "release") (release-experience (rest args))
        (= command "serve") (start-server (rest args))
        (= command "help") (show-help)
            :else (show-help)))))
