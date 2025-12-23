package com.ravi.hogwartsartifact.wizard;

import com.ravi.hogwartsartifact.system.Result;

import com.ravi.hogwartsartifact.system.StatusCode;
import com.ravi.hogwartsartifact.wizard.convertor.WizardDtoToWizardConverter;
import com.ravi.hogwartsartifact.wizard.convertor.WizardToWizardDtoConvertor;
import com.ravi.hogwartsartifact.wizard.dto.WizardDto;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoints.base-url}/wizards")
public class WizardController {

    private final WizardService wizardService;
    private final WizardToWizardDtoConvertor wizardToWizardDtoConvertor;
    private final WizardDtoToWizardConverter wizardDtoToWizardConvertor;

    public WizardController(
            WizardService wizardService,
            WizardToWizardDtoConvertor wizardToWizardDtoConvertor,
            WizardDtoToWizardConverter wizardDtoToWizardConvertor) {
        this.wizardService = wizardService;
        this.wizardToWizardDtoConvertor = wizardToWizardDtoConvertor;
        this.wizardDtoToWizardConvertor = wizardDtoToWizardConvertor;
    }

    @GetMapping("/{wizardId}")
    public Result findWizardById(@PathVariable Integer wizardId){
        Wizard foundWizard = this.wizardService.findById(wizardId);
        WizardDto foundWizardDto = this.wizardToWizardDtoConvertor.convert(foundWizard);
        return new Result(true,StatusCode.SUCCESS,"Find One Success",foundWizardDto);
    }

    @GetMapping
    public Result findAllWizard(){
        List<Wizard> wizardServiceAll = wizardService.findAll();
        List<@Nullable WizardDto> listOfWizardDto = wizardServiceAll.stream().map(this.wizardToWizardDtoConvertor::convert).toList();
        return new Result(true, StatusCode.SUCCESS,"Find All Success",listOfWizardDto);
    }

    @PostMapping
    public Result addWizard(@RequestBody
                            @Valid WizardDto wizardDto){
        Wizard convertedWizard = this.wizardDtoToWizardConvertor.convert(wizardDto);
        Wizard savedWizard = this.wizardService.save(convertedWizard);
        WizardDto savedWizardDto = this.wizardToWizardDtoConvertor.convert(savedWizard);
        return new Result(true,StatusCode.SUCCESS,"Add Success",savedWizardDto);
    }

    @PutMapping("/{wizardId}")
    public Result updateWizard(@RequestBody @Valid WizardDto wizardDto,
                               @PathVariable Integer wizardId){
        Wizard convertedWizard = this.wizardDtoToWizardConvertor.convert(wizardDto);
        Wizard updatedWizard = this.wizardService.updateWizard(convertedWizard, wizardId);
        WizardDto updatedWizardDto = this.wizardToWizardDtoConvertor.convert(updatedWizard);
        return new Result(true,StatusCode.SUCCESS,"Update Success",updatedWizardDto);
    }

    @DeleteMapping("/{wizardId}")
    public Result deleteWizard(@PathVariable Integer wizardId){
        this.wizardService.deleteWizard(wizardId);
        return new Result(true,StatusCode.SUCCESS,"Delete Success");
    }

    @PutMapping("{wizardId}/artifacts/{artifactId}")
    public Result assignArtifactToWizard(
            @PathVariable Integer wizardId,
            @PathVariable String artifactId){
        this.wizardService.assignArtifact(wizardId,artifactId);
        return new Result(true,StatusCode.SUCCESS,"Artifact Assignment Success");
    }



}
