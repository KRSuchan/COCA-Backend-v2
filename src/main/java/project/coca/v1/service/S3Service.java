package project.coca.v1.service;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
public class S3Service {
    private final S3Operations s3Operations;
    private final String profileFolderPath = "profile-images/";
    private final String groupFolderPath = "groups/";
    private final String personalFolderPath = "personals/";
    @Value("${spring.cloud.aws.s3.bucket}")
    private String BUCKET;

    public S3Service(S3Operations s3Operations) {
        this.s3Operations = s3Operations;
    }

    private URL uploadFile(MultipartFile file, String key) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            S3Resource s3Resource = s3Operations.upload(BUCKET, key, inputStream, ObjectMetadata.builder().contentType(file.getContentType()).build());
            return s3Resource.getURL();
        } catch (IOException e) {
            throw new IOException("IO EXCEPTION IN S3Service.uploadFile()");
        }
    }

    @Transactional
    public URL uploadProfileImage(MultipartFile multipartFile, String memberId) throws IOException {
        if (!MediaType.IMAGE_PNG.toString().equals(multipartFile.getContentType()) &&
                !MediaType.IMAGE_JPEG.toString().equals(multipartFile.getContentType())) {
            System.out.println("png, jpeg 파일만 업로드 가능합니다");
            throw new IllegalArgumentException("png, jpeg 파일만 업로드 가능합니다");
        }

        return uploadFile(multipartFile, profileFolderPath + memberId);
    }

    /**
     * @param multipartFile      : 원본 파일
     * @param memberId           : 회원 계정
     * @param personalScheduleId : 회원의 개인 일정 id
     * @param divisionNum        : 분류 번호 ex) 파일 명 (1).png 형태로 저장, 0이면 없음
     * @return AWS에 저장된 파일의 URL
     * @throws IOException
     */
    @Transactional
    public URL uploadPersonalScheduleFile(MultipartFile multipartFile, String memberId, Long personalScheduleId, int divisionNum) throws IOException {
        String divider = "";
        if (divisionNum != 0) {
            divider = "(" + divisionNum + ") ";
        }
        return uploadFile(multipartFile, personalFolderPath + memberId + "/" + personalScheduleId + "/" + divider + multipartFile.getOriginalFilename());
    }

    /**
     * @param multipartFile   : 원본 파일
     * @param groupId         : 그룹 id
     * @param groupScheduleId : 그룹 일정 id
     * @param divisionNum     : 분류 번호를 직접 지정(1, 2, 3 등) ex) "(1) 파일 명.png" 형태로 저장, 0이면 없음
     * @return AWS에 저장된 파일의 URL
     * @throws IOException
     */
    @Transactional
    public URL uploadGroupScheduleFile(MultipartFile multipartFile, Long groupId, Long groupScheduleId, int divisionNum) throws IOException {
        String divider = "";
        if (divisionNum != 0) {
            divider = "(" + divisionNum + ") ";
        }
        return uploadFile(multipartFile, groupFolderPath + groupId + "/" + groupScheduleId + "/" + divider + multipartFile.getOriginalFilename());
    }

    /**
     * @param url : 파일이 저장된 url
     */
    @Transactional
    public void deleteS3File(String url) {
        String key = url.replace("https://cocaattachments.s3.amazonaws.com/", "");
        System.out.println(key);
        s3Operations.deleteObject(BUCKET, key);
    }
}
