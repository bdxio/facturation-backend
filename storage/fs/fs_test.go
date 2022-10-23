package fs

import (
	"fmt"
	"os"
	"path/filepath"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
)

func TestStorage_Exists(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name       string
		directory  string
		filename   string
		wantExists bool
		wantErr    error
	}{
		{
			name:       "file exists",
			directory:  "testdata",
			filename:   "exists.pdf",
			wantExists: true,
			wantErr:    nil,
		},
		{
			name:       "file don't exist",
			directory:  "testdata",
			filename:   "dont-exists.pdf",
			wantExists: false,
			wantErr:    nil,
		},
	}

	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			fs := NewStorage(tt.directory)
			exists, err := fs.Exists(tt.filename)

			assert.Equal(t, tt.wantExists, exists)
			assert.Equal(t, tt.wantErr, err)
		})
	}
}

func TestStorage_Save(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name      string
		directory string
		filename  string
		data      []byte
		wantErr   string
	}{
		{
			name:      "save file",
			directory: os.TempDir(),
			filename:  fmt.Sprintf("file-%s.txt", time.Now().Format("20060102150405")),
			data:      []byte("test"),
			wantErr:   "",
		},
		{
			name:      "cannot save file",
			directory: "this/directory/does/not/exist",
			filename:  "should-not-be-saved.txt",
			data:      []byte("test"),
			wantErr:   "open this/directory/does/not/exist/should-not-be-saved.txt: no such file or directory",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			fs := NewStorage(tt.directory)

			err := fs.Save(tt.filename, "mimeType", tt.data)

			if tt.wantErr != "" {
				assert.EqualError(t, err, tt.wantErr)
				return
			}

			data, err := os.ReadFile(filepath.Join(tt.directory, tt.filename))

			assert.NoError(t, err)
			assert.Equal(t, tt.data, data)
		})
	}
}
