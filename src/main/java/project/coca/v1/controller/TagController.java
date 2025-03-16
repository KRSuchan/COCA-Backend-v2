package project.coca.v1.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.coca.domain.tag.Tag;
import project.coca.v1.dto.response.common.ApiResponse;
import project.coca.v1.dto.response.common.success.ResponseCode;
import project.coca.v1.dto.response.tag.TagResponse;
import project.coca.v1.repository.TagRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/tag")
public class TagController {
    private final TagRepository tagRepository;

    /**
     * a. 모든 태그 조회
     */
    @GetMapping("/all")
    public ApiResponse<List<TagResponse>> findGroupsByGroupName() {
        List<Tag> tags = tagRepository.findAll();
        List<TagResponse> data = tags
                .stream()
                .map(TagResponse::of)
                .collect(Collectors.toList());
        return ApiResponse.response(ResponseCode.OK, data);
    }
}
