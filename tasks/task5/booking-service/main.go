package main

import (
	"fmt"
	"log"
	"net/http"
	"os"
)

func main() {
	enableFeatureX := os.Getenv("ENABLE_FEATURE_X") == "true"
	imageVersion := os.Getenv("IMAGE_VERSION")

	http.HandleFunc("/ping", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintf(w, "pong %s", imageVersion)
	})

	// TODO: Feature flag route
	// if ENABLE_FEATURE_X=true, expose /feature
	if enableFeatureX {
		http.HandleFunc("/feature", func(w http.ResponseWriter, r *http.Request) {
			fmt.Fprintf(w, "Feature X is enabled!")
		})
	}

	log.Println("Server running on :8080")
	log.Fatal(http.ListenAndServe(":8080", nil))
}
