package logo

import (
	"encoding/base64"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestDownloader_Download(t *testing.T) {
	t.Parallel()

	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
		t.Helper()

		w.WriteHeader(http.StatusOK)
		_, err := w.Write([]byte("logo"))
		require.NoError(t, err)
	}))
	defer srv.Close()

	downloader := NewDownloader(srv.Client(), io.ReadAll)

	logo, err := downloader.Download(srv.URL)
	require.NoError(t, err)

	l, err := base64.StdEncoding.DecodeString(logo)
	require.NoError(t, err)
	assert.Equal(t, []byte("logo"), l)
}

func TestDownloader_DownloadErrors(t *testing.T) {
	t.Parallel()

	tests := []struct {
		name    string
		handler func(w http.ResponseWriter, req *http.Request)
		url     func(s *httptest.Server) string
		reader  Reader
		wantErr string
	}{
		{
			name: "http error code",
			handler: func(w http.ResponseWriter, req *http.Request) {
				w.WriteHeader(http.StatusInternalServerError)
			},
			url:     func(s *httptest.Server) string { return s.URL },
			reader:  io.ReadAll,
			wantErr: `could not download http:\/\/127\.0\.0\.1:[\d]{5}: 500`,
		},
		{
			name:    "invalid URL",
			handler: func(w http.ResponseWriter, req *http.Request) {},
			url:     func(s *httptest.Server) string { return "protocol://invalid URL" },
			reader:  io.ReadAll,
			wantErr: `parse "protocol://invalid URL": invalid character " " in host name`,
		},
		{
			name:    "error while reading body",
			handler: func(w http.ResponseWriter, req *http.Request) {},
			url:     func(s *httptest.Server) string { return s.URL },
			reader:  func(_ io.Reader) ([]byte, error) { return nil, errors.New("reader error") },
			wantErr: "reader error",
		},
	}
	for _, tt := range tests {
		tt := tt
		t.Run(tt.name, func(t *testing.T) {
			t.Parallel()

			srv := httptest.NewServer(http.HandlerFunc(tt.handler))
			defer srv.Close()

			downloader := NewDownloader(srv.Client(), tt.reader)

			_, err := downloader.Download(tt.url(srv))

			require.Error(t, err)
			assert.Regexp(t, tt.wantErr, err.Error())
		})
	}
}
