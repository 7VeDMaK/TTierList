package adlist.Controller;

import adlist.Entity.Card;
import adlist.Enum.Rank;
import adlist.Repository.CardRepository;
import adlist.Service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CardImageCotroller {

    private final CardService service;

    private final CardRepository cardRepository;

    public CardImageCotroller(CardService service, CardRepository cardRepository) {
        this.service = service;
        this.cardRepository = cardRepository;
    }

    @PostMapping("/fileSystem")
    public ResponseEntity<?> uploadImageToFIleSystem(@RequestParam("image") MultipartFile file) throws IOException {
//        service.resizeImage();
        String uploadImage = service.uploadImageToFileSystem(file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadImage);
    }

    @PostMapping("/fileSystemMany")
    public String uploadAndCreateCard(@RequestParam("images") MultipartFile[] files) throws IOException {
        List<String> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = service.uploadImageToFileSystem(file);
            if (fileName != null) {

                Card card = new Card();
                card.setName(fileName.substring(0, fileName.lastIndexOf('.'))); // Имя файла без расширения
                card.setPosition(0);
                card.setRank(Rank.UNDEFINED); // Убедись, что у тебя есть enum Rank с таким значением
                card.setImageUrl(fileName);

                cardRepository.save(card);
                uploadedFiles.add(fileName);
            }
        }

        return "redirect:/cards";
    }


    @GetMapping("/fileSystem/{fileName}")
    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String fileName) throws IOException {
        byte[] imageData = service.downloadImageFromFileSystem(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);

    }
}
