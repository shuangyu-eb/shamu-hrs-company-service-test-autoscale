package shamu.company.common.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import shamu.company.common.entity.BaseEntity;

@MapperConfig(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface Config {

  @Mapping(target = "id", ignore = true)
  BaseEntity convertToEntity(Object dto);
}
