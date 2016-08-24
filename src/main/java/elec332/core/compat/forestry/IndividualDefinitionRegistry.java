package elec332.core.compat.forestry;

import com.google.common.collect.Lists;
import elec332.core.main.ElecCore;
import forestry.api.genetics.*;

import java.util.List;

/**
 * Created by Elec332 on 15-8-2016.
 */
public class IndividualDefinitionRegistry {

    public static <O extends Enum<O> & IIndividualTemplate<T, B, ?, ?, ?, ?>, T extends IGenomeTemplate<S>, S extends IAlleleSpecies, B extends IAlleleSpeciesBuilder> void registerBees(Class<O> clazz) {
        for (O o : clazz.getEnumConstants()){
            registerBee(o);
        }
    }

    public static <T extends IGenomeTemplate<S>, S extends IAlleleSpecies, B extends IAlleleSpeciesBuilder> void registerBee(IIndividualTemplate<T, B, ?, ?, ?, ?> template) {
        if (locked){
            throw new IllegalStateException("You cannot register bees in postInit!");
        }
        T genomeTemplate;
        try {
            genomeTemplate = template.getGenomeTemplateType().newInstance();
        } catch (Exception e){
            ElecCore.logger.error("Error creating Genome-Template of type: "+template.getGenomeTemplateType().getCanonicalName());
            return;
        }
        template.getIndividualBranch().setBranchProperties(genomeTemplate);
        ISpeciesRoot speciesRoot = template.getSpeciesRoot();
        B speciesBuilder = template.getSpeciesBuilder();
        template.setSpeciesProperties(speciesBuilder);
        @SuppressWarnings("unchecked")
        S species = (S) speciesBuilder.build();
        genomeTemplate.setSpecies(species);
        template.modifyGenomeTemplate(genomeTemplate);
        IAllele[] alleles = genomeTemplate.getAlleles();
        speciesRoot.registerTemplate(alleles);
        template.setIndividualDefinition(new DefaultIndividualDefinition<>(alleles, speciesRoot));
        templates.add(template);
    }

    static final List<IIndividualTemplate> templates;
    static boolean locked;

    static {
        locked = false;
        templates = Lists.newArrayList();
    }

}
