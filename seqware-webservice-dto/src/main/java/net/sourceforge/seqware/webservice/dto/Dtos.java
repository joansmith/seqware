package net.sourceforge.seqware.webservice.dto;

import net.sourceforge.seqware.common.model.Attribute;
import net.sourceforge.seqware.common.model.File;
import net.sourceforge.seqware.common.model.IUS;
import net.sourceforge.seqware.common.model.Organism;
import net.sourceforge.seqware.common.model.Registration;
import net.sourceforge.seqware.common.model.Sample;
import net.sourceforge.seqware.common.model.SampleAttribute;
import net.sourceforge.seqware.common.model.SampleSearch;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Methods to convert between domain objects and dtos.
 * 
 */
public final class Dtos {

	public static SampleSearchDto asDto(SampleSearch from) {
		SampleSearchDto dto = new SampleSearchDto();
		return dto;
	}

	// Todo remove this one after changing units method name.
	public static AttributeDto sampleAttributeAsDto(SampleAttribute from) {
		AttributeDto dto = new AttributeDto();
		dto.setName(from.getTag());
		dto.setValue(from.getValue());
		dto.setUnit(from.getUnits());
		return dto;
	}

	public static AttributeDto asDto(Attribute from) {
		AttributeDto dto = new AttributeDto();
		dto.setName(from.getTag());
		dto.setValue(from.getValue());
		dto.setUnit(from.getUnit());
		return dto;
	}

	public static <T extends Attribute> T fromDto(AttributeDto attributeDto, Class<T> clazz)
			throws InstantiationException, IllegalAccessException {
		T attribute = clazz.newInstance();
		attribute.setTag(attributeDto.getName());
		attribute.setValue(attributeDto.getValue());
		if (attributeDto.getUnit() != null) {
			attribute.setUnit(attributeDto.getUnit());
		}
		return attribute;
	}

	public static OwnerDto asDto(Registration from) {
		OwnerDto dto = new OwnerDto();
		dto.setEmail(from.getEmailAddress());
		dto.setFirstName(from.getFirstName());
		dto.setLastName(from.getLastName());
		dto.setInstitution(from.getInstitution());
		return dto;
	}

	public static OrganismDto atDto(Organism from) {
		OrganismDto dto = new OrganismDto();
		dto.setName(from.getName());
		dto.setCode(from.getCode());
		dto.setNcbiTaxonomyId(from.getNcbiTaxId());
		return dto;
	}

	public static LibraryDto asDto(Sample sample) {
		DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
		LibraryDto dto = new LibraryDto();
		dto.setName(sample.getName());
		dto.setDescription(sample.getDescription());
		dto.setCreateTimeStamp(dateTimeFormatter.print(sample.getCreateTimestamp().getTime()));
		dto.setUpdateTimeStamp(dateTimeFormatter.print(sample.getUpdateTimestamp().getTime()));
		dto.setOwner(Dtos.asDto(sample.getOwner()));
		dto.setOrganism(Dtos.atDto(sample.getOrganism()));

		return dto;
	}

	public static IusDto asDto(IUS ius) {
		DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
		IusDto dto = new IusDto();
		dto.setSwa(ius.getSwAccession());
		dto.setSkip(ius.getSkip());
		dto.setCreateTimeStamp(dateTimeFormatter.print(ius.getCreateTimestamp().getTime()));
		dto.setUpdateTimeStamp(dateTimeFormatter.print(ius.getUpdateTimestamp().getTime()));
		if (ius.getTag() != null) {
			dto.setBarcode(ius.getTag());
		}
		return dto;
	}

	public static FileDto asDto(File file) {
		FileDto dto = new FileDto();
		dto.setFilePath(file.getFilePath());
		if (file.getMetaType() != null) {
			dto.setMetaType(file.getMetaType());
		}
		if (file.getDescription() != null) {
			dto.setDescription(file.getDescription());
		}
		if (file.getMd5sum() != null) {
			dto.setMd5sum(file.getMd5sum());
		}
		if (file.getSize() != null) {
			dto.setSize(file.getSize());
		}
		if (file.getType() != null) {
			dto.setType(file.getType());
		}
		return dto;
	}
}
