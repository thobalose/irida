const angular = require('angular');
require('angular-resource');
import {metadataTemplateName} from './directives/metadataTemplateName';
import {SampleMetadataTemplateService} from './services/sample-metadata-template.service';

export const SampleMetadataTemplateModule =
  angular.module('irida.samples.metadata.template', ['ngResource'])
    .directive('metadataTemplateName', metadataTemplateName)
    .service('SampleMetadataTemplateService', SampleMetadataTemplateService)
    .name;