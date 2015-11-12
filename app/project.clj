(defproject app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [markdown-clj "0.9.77"]
                 [io.aviso/pretty "0.1.19"]
                 [org.clojure/tools.logging "0.3.1"]
                 [me.raynes/fs "1.4.6"]
                 [cheshire "5.5.0"]
                 [clj-time "0.11.0"]]
  :plugins [[lein-environ "1.0.0"]]
  :profiles {:dev {:env {:squiggly {:checkers [:eastwood]
                                    :eastwood-exclude-linters [:unlimited-use]}}}}
  :env {:squiggly {:checkers [:eastwood]
                   :eastwood-exclude-linters [:unlimited-use]}}
  :main app.core
  :aot [app.core]
  :jvm-opts ^:replace []
  :uberjar {:aot :all})
