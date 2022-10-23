package fs

import (
	"os"
	"path/filepath"
)

type Storage struct {
	directory string
}

func NewStorage(directory string) Storage {
	return Storage{directory: directory}
}

func (s Storage) Exists(filename string) (bool, error) {
	path := filepath.Join(s.directory, filename)
	_, err := os.Stat(path)
	if err == nil {
		return true, nil
	}
	if os.IsNotExist(err) {
		return false, nil
	}
	return false, err
}

func (s Storage) Save(filename, mimeType string, data []byte) error {
	path := filepath.Join(s.directory, filename)
	return os.WriteFile(path, data, 0o0644)
}
