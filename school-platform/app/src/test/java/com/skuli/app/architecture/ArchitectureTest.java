package com.skuli.app.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the modular-monolith boundaries in CI. These rules pass trivially in Phase 1 (modules
 * have no domain classes yet) and become load-bearing in Phase 2/3 as modules gain
 * {@code api/}/{@code internal/} packages. A failing test here outranks any architecture document.
 */
class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.skuli");

    @Test
    void controllersMustNotDependOnRepositories() {
        ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("Controller")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
                .allowEmptyShould(true);
        rule.check(classes);
    }

    @Test
    void internalPackagesMustNotBeAccessedFromOtherModules() {
        // A module's internals are only reachable within its own module (or the shared kernel).
        ArchRule rule = classes()
                .that().resideInAPackage("..internal..")
                .should().onlyBeAccessed().byClassesThat()
                .resideInAnyPackage("..internal..", "com.skuli.common..")
                .allowEmptyShould(true);
        rule.check(classes);
    }

    @Test
    void commonLibraryMustNotDependOnFeatureModules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.skuli.common..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.skuli.auth..",
                        "com.skuli.student..",
                        "com.skuli.staff..",
                        "com.skuli.academics..",
                        "com.skuli.finance..",
                        "com.skuli.communication..",
                        "com.skuli.reporting..",
                        "com.skuli.aiclient..")
                .allowEmptyShould(true);
        rule.check(classes);
    }
}
