(ns formal-ventus.example.core
  (:require [formal-ventus.example.views :as views]
            [reagent.dom :as dom]))

(defn ^:dev/after-load mount-root []
  (dom/render [views/main-panel] (.getElementById js/document "app")))

(defn ^:export init []
  (enable-console-print!)
  (mount-root))
