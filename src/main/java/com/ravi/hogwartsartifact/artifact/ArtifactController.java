package com.ravi.hogwartsartifact.artifact;

import com.ravi.hogwartsartifact.artifact.convertor.ArtifactDtoToArtifactConvertor;
import com.ravi.hogwartsartifact.artifact.convertor.ArtifactToArtifactDtoConvertor;
import com.ravi.hogwartsartifact.artifact.dto.ArtifactDto;
import com.ravi.hogwartsartifact.system.Result;
import com.ravi.hogwartsartifact.system.StatusCode;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("${api.endpoints.base-url}/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;
    private final ArtifactToArtifactDtoConvertor artifactToArtifactDtoConvertor;
    private final ArtifactDtoToArtifactConvertor artifactDtoToArtifactConvertor;

    Logger logger = LoggerFactory.getLogger(ArtifactController.class);

    public ArtifactController(ArtifactService artifactService, ArtifactToArtifactDtoConvertor artifactToArtifactDtoConvertor, ArtifactDtoToArtifactConvertor artifactDtoToArtifactConvertor) {
        this.artifactService = artifactService;
        this.artifactToArtifactDtoConvertor = artifactToArtifactDtoConvertor;
        this.artifactDtoToArtifactConvertor = artifactDtoToArtifactConvertor;
    }

    @GetMapping("/{artifactId}")
    public Result findArtifactById(@PathVariable String artifactId){
        Artifact foundArtifact = this.artifactService.findById(artifactId);
        ArtifactDto artifactDto = artifactToArtifactDtoConvertor.convert(foundArtifact);
        Result findOneSuccess = new Result(true, StatusCode.SUCCESS, "Find One Success", artifactDto);
        logger.info("printing log before returning back form /artifact/{artifactId} api and dto api {},{}",findOneSuccess,artifactDto);
        return findOneSuccess;
    }

    @GetMapping
    public Result findAllArtifacts(){
        List<Artifact> foundArtifacts = this.artifactService.findAll();
        List<ArtifactDto> allArtifact = foundArtifacts.stream().map(this.artifactToArtifactDtoConvertor::convert).toList();
        return new Result(true,StatusCode.SUCCESS,"Find All Success",allArtifact);
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
}
