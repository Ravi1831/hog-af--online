package com.ravi.hogwartsartifact.artifact;

import com.ravi.hogwartsartifact.artifact.convertor.ArtifactDtoToArtifactConvertor;
import com.ravi.hogwartsartifact.artifact.convertor.ArtifactToArtifactDtoConvertor;
import com.ravi.hogwartsartifact.artifact.dto.ArtifactDto;
import com.ravi.hogwartsartifact.client.imageStorage.ImageStorageClient;
import com.ravi.hogwartsartifact.system.Result;
import com.ravi.hogwartsartifact.system.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("${api.endpoints.base-url}/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;
    private final ArtifactToArtifactDtoConvertor artifactToArtifactDtoConvertor;
    private final ArtifactDtoToArtifactConvertor artifactDtoToArtifactConvertor;
    private final MeterRegistry meterRegistry;
    private final ImageStorageClient imageStorageClient;

    Logger logger = LoggerFactory.getLogger(ArtifactController.class);

    public ArtifactController(ArtifactService artifactService, ArtifactToArtifactDtoConvertor artifactToArtifactDtoConvertor, ArtifactDtoToArtifactConvertor artifactDtoToArtifactConvertor, MeterRegistry meterRegistry, ImageStorageClient imageStorageClient) {
        this.artifactService = artifactService;
        this.artifactToArtifactDtoConvertor = artifactToArtifactDtoConvertor;
        this.artifactDtoToArtifactConvertor = artifactDtoToArtifactConvertor;
        this.meterRegistry = meterRegistry;
        this.imageStorageClient = imageStorageClient;
    }

    @GetMapping("/summary")
    public Result summarizeArtifact(){
        List<Artifact> foundArtifacts = this.artifactService.findAll();
        List<@Nullable ArtifactDto> artifactsDto = foundArtifacts.stream()
                .map(this.artifactToArtifactDtoConvertor::convert)
                .toList();
        String artifactSummary = this.artifactService.summarize(artifactsDto);
        return new Result(true,StatusCode.SUCCESS,"Summarize Success",artifactSummary);
    }

    @GetMapping("/{artifactId}")
    public Result findArtifactById(@PathVariable String artifactId){
        Artifact foundArtifact = this.artifactService.findById(artifactId);
        meterRegistry.counter("artifact.id."+artifactId).increment();
        ArtifactDto artifactDto = artifactToArtifactDtoConvertor.convert(foundArtifact);
        Result findOneSuccess = new Result(true, StatusCode.SUCCESS, "Find One Success", artifactDto);
        logger.info("printing log before returning back form /artifact/{artifactId} api and dto api {},{}",findOneSuccess,artifactDto);
        return findOneSuccess;
    }

    @GetMapping
    public Result findAllArtifacts(Pageable pageable){
        Page<Artifact> artifactPage = this.artifactService.findAll(pageable);
        Page<ArtifactDto> artifactDtoPage = artifactPage.map(this.artifactToArtifactDtoConvertor::convert);
        return new Result(true,StatusCode.SUCCESS,"Find All Success",artifactDtoPage);
    }

    @PostMapping
    public Result addArtifact(@RequestBody
                                  @Valid ArtifactDto artifactDto){
        Artifact newArtifact = this.artifactDtoToArtifactConvertor.convert(artifactDto);
        Artifact savedArtifact = this.artifactService.save(newArtifact);
        ArtifactDto savedDto = this.artifactToArtifactDtoConvertor.convert(savedArtifact);
        return new Result(true,StatusCode.SUCCESS,"Add Success",savedDto);
    }

    @PutMapping("/{artifactId}")
    public Result updateArtifacts(
            @PathVariable String artifactId,
            @RequestBody @Valid ArtifactDto artifactDto){
        Artifact update = artifactDtoToArtifactConvertor.convert(artifactDto);
        Artifact updatedArtifacts = this.artifactService.update(artifactId, update);
        ArtifactDto updatedArtifactDto = artifactToArtifactDtoConvertor.convert(updatedArtifacts);
        return new Result(true,StatusCode.SUCCESS,"Update Success",updatedArtifactDto);
    }

    @DeleteMapping("/{artifactId}")
    public Result deleteArtifacts(@PathVariable String artifactId){
        this.artifactService.delete(artifactId);
        return new Result(true,StatusCode.SUCCESS,"Delete Success");
    }

    @PostMapping("/search")
    public Result findArtifactByCriteria(
            @RequestBody Map<String,String> searchCriteria,
            Pageable pageable
            ){
        Page<Artifact> artifactPage = this.artifactService.findByCriteria(searchCriteria,pageable);
        Page<ArtifactDto> artifactDtoPage = artifactPage.map(this.artifactToArtifactDtoConvertor::convert);
        return new Result(true,StatusCode.SUCCESS,"Search Success",artifactDtoPage);
    }

    @PostMapping("/images")
    public Result uploadImage(
            @RequestParam String  containerName,
            @RequestParam MultipartFile file) throws IOException {
        try(InputStream inputStream = file.getInputStream()){
            String imageUrl = this.imageStorageClient.uploadImage(
                    containerName,
                    file.getOriginalFilename(),
                    inputStream, file.getSize()
            );
            return new Result(true,StatusCode.SUCCESS,"Upload Image Success",imageUrl);
        }
    }
}
