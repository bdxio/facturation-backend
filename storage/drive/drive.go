package drive

import (
	"bytes"
	"context"
	"fmt"
	"net/http"

	"google.golang.org/api/drive/v2"
	"google.golang.org/api/option"
)

type Storage struct {
	srv      *drive.Service
	folderID string
	files    map[string]struct{}
}

func NewStorage(client *http.Client, folderID string) (Storage, error) {
	srv, err := drive.NewService(context.Background(), option.WithHTTPClient(client))
	if err != nil {
		return Storage{}, err
	}

	files, err := listFiles(srv, folderID)
	if err != nil {
		return Storage{}, fmt.Errorf("could not list files: %w", err)
	}

	return Storage{srv: srv, folderID: folderID, files: files}, nil
}

func (s Storage) Exists(filename string) (bool, error) {
	_, ok := s.files[filename]
	return ok, nil
}

func (s Storage) Save(filename, mimeType string, data []byte) error {
	file := drive.File{
		Title:    filename,
		MimeType: mimeType,
		Parents:  []*drive.ParentReference{{Id: s.folderID}},
	}
	_, err := s.srv.Files.Insert(&file).SupportsTeamDrives(true).Media(bytes.NewReader(data)).Do()
	return err
}

func listFiles(srv *drive.Service, folderID string) (map[string]struct{}, error) {
	query := fmt.Sprintf("'%s' in parents", folderID)
	var nextPageToken string
	files := make(map[string]struct{})

	for {
		list, err := srv.Files.List().Q(query).Spaces("drive").Fields("nextPageToken", "items(title)").SupportsTeamDrives(true).IncludeTeamDriveItems(true).PageToken(nextPageToken).Do()
		if err != nil {
			return nil, err
		}
		for _, item := range list.Items {
			files[item.Title] = struct{}{}
		}
		nextPageToken = list.NextPageToken
		if nextPageToken == "" {
			break
		}
	}

	return files, nil
}
