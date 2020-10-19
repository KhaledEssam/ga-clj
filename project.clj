(defproject ga-clj "0.1.0-SNAPSHOT"
  :description "Genetic Algorithm in Clojure"
  :url "http://github.com/KhaledEssam/ga-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [progrock "0.1.2"]
                 [metasoarous/oz "1.6.0-alpha30"]
                 [org.clojure/tools.cli "1.0.194"]]
  :main ^:skip-aot ga-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
