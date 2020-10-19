(ns ga-clj.core
  (:require [ga-clj.genetic :as g]
            [oz.core :as oz]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))


(defn make-config [{:keys [num-cities
                           population-size
                           num-generations
                           mutation-rate
                           elite-size]}]
  {:cities (g/rand-coord num-cities)
   :population-size population-size
   :num-generations num-generations
   :mutation-rate mutation-rate
   :elite-size elite-size})


(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 5050
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]

   ["-c" "--num-cities NUM_CITIES" "Number of cities"
    :default 15
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 100) "Must be a number between 0 and 100"]]

   ["-ps" "--population-size POPULATION_SIZE" "Population size"
    :default 150
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 1000) "Must be a number between 0 and 1000"]]

   ["-g" "--num-generations NUM_GENERATIONS" "Number of generations"
    :default 500
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 20000) "Must be a number between 0 and 20000"]]

   ["-m" "--mutation-rate MUTATION_RATE" "Mutation rate"
    :default 0.01
    :parse-fn #(Double/parseDouble %)
    :validate [#(< 0 % 1) "Must be a number between 0 and 1"]]

   ["-e" "--elite-size ELITE_SIZE" "Elite size"
    :default 20
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 100) "Must be a number between 0 and 100"]]])


(defn -main
  [& args]
  (let [opts (:options (parse-opts args cli-options))
        config (make-config opts)
        fitnesses (g/genetic-algorithm config)
        line-plot {:layer g/layer
                   :width 1600
                   :height 800
                   :data {:values (g/vega-data fitnesses)}
                   :mark "line"
                   :encoding g/encoding}
        _ (oz/start-server! 5050)]
    (oz/view! line-plot)))
