package org.trustoverip.ctwg.toolkit.mrg.model;

import java.util.List;

/**
 * @author sih
 */
public record MRGModel(Terminology terminology, List<ScopeRef> scopes, List<MRGEntry> entries) {

}
