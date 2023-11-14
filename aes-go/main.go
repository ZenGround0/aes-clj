package main

import (
	"fmt"
	"encoding/hex"
	"encoding/binary"
	"net/http"
	"io/ioutil"
	cacheclear "github.com/zenground0/aes-clj/aes-go/cacheclear"
)

func aesEnc(hexPtext string, keySchedule []uint32) ([]byte, uint64) {
	src, err := hex.DecodeString(hexPtext)
	if err != nil {
		panic(err)
	}


	start := cacheclear.Cputicks()
	encryptBlockGo(keySchedule, src, src)
	duration := cacheclear.Cputicks() - start
	return src, duration
}

func main2() {
	key, err := hex.DecodeString("000102030405060708090a0b0c0d0e0f")
	if err != nil {
		panic(err)
	}
	n := len(key) + 28
	enc, dec  := make([]uint32, n), make([]uint32, n)
	expandKeyGo(key, enc, dec)
	keyFmt := ""
	for _, i := range enc {
		buf := make([]byte, 4)
		binary.BigEndian.PutUint32(buf, i)
		keyFmt += fmt.Sprintf("\n%x", buf)
	}
	fmt.Printf("Key schedule: %s\n", keyFmt)
		
	http.HandleFunc("/hex", handleHexWithKey(enc))

	fmt.Println("Server is listening on port 8080...")
	err = http.ListenAndServe(":8080", nil)
	if err != nil {
		fmt.Println("Error starting server:", err)
	}
	
}

func handleHexWithKey(keySchedule []uint32) func (http.ResponseWriter, *http.Request) {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "Invalid request method", http.StatusMethodNotAllowed)
			return
		}

		// Read the body of the request
		body, err := ioutil.ReadAll(r.Body)
		if err != nil {
			http.Error(w, "Error reading request body", http.StatusInternalServerError)
			return
		}
		defer r.Body.Close()

		// Convert body to string assuming it is in hex format
		cacheclear.ClearL3()
		ptext := string(body)
		ctext, duration := aesEnc(ptext, keySchedule)

		// Write a response
		fmt.Fprintf(w, "\"Ciphertext: %x, Elapsed time: %d cputicks\"\n", ctext, duration)
	}
}

func main() {
	main2()
}
