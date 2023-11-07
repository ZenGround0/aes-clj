package main

import (
	"fmt"
	"encoding/hex"
	"encoding/binary"	
)

func main() {
	key, err := hex.DecodeString("000102030405060708090a0b0c0d0e0f")
	if err != nil {
		panic(err)
	}
	src, err := hex.DecodeString("00112233445566778899aabbccddeeff")
	if err != nil {
		panic(err)
	}

	n := len(key) + 28
	enc, dec  := make([]uint32, n), make([]uint32, n)
	expandKeyGo(key, enc, dec)
	encryptBlockGo(enc, src, src)
	fmt.Printf("%x", src)
}
