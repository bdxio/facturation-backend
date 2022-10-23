package logo

import (
	"encoding/base64"
	"fmt"
	"io"
	"net/http"
)

type Downloader struct {
	client *http.Client
	reader Reader
}

type Reader func(r io.Reader) ([]byte, error)

func NewDownloader(c *http.Client, r Reader) Downloader {
	return Downloader{client: c, reader: r}
}

func (d Downloader) Download(url string) (string, error) {
	resp, err := d.client.Get(url)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	body, err := d.reader(resp.Body)
	if err != nil {
		return "", err
	}

	if resp.StatusCode >= http.StatusBadRequest {
		return "", fmt.Errorf("could not download %s: %v (%s)", url, resp.StatusCode, string(body))
	}

	return base64.StdEncoding.EncodeToString(body), nil
}
