package com.oracle.javafx.scenebuilder.kit.editor.panel.library.maven;

import com.oracle.javafx.scenebuilder.kit.editor.panel.library.maven.repository.Repository;
import com.oracle.javafx.scenebuilder.app.AppPlatform;
import com.oracle.javafx.scenebuilder.app.i18n.I18N;
import com.oracle.javafx.scenebuilder.app.preferences.PreferencesController;
import com.oracle.javafx.scenebuilder.kit.editor.panel.library.maven.preset.MavenPresets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.metadata.DefaultMetadata;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.version.Version;

public class MavenRepositorySystem {

    // TODO: Manage List of Repositories
    // TODO: Manage private repositories and credentials
    
    private RepositorySystem system;
    
    private DefaultRepositorySystemSession session;
    
    private LocalRepository localRepo;
    
    private VersionRangeResult rangeResult;
    
    private final boolean onlyReleases;
    
    private BasicRepositoryConnectorFactory basicRepositoryConnectorFactory;
    
    public MavenRepositorySystem(boolean onlyReleases) {
        this.onlyReleases = onlyReleases;
        initRepositorySystem();
    }
    
    private void initRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                throw new RuntimeException(I18N.getString("maven.dialog.creation.failed"), exception);
            }
        });
        
        basicRepositoryConnectorFactory = new BasicRepositoryConnectorFactory();
        basicRepositoryConnectorFactory.initService(locator);
        
        system = locator.getService(RepositorySystem.class);

        session = MavenRepositorySystemUtils.newSession();

        localRepo = new LocalRepository(new File(AppPlatform.getUserM2Repository()));
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        // TODO: log file transfers
        session.setTransferListener(new AbstractTransferListener() {
            @Override
            public void transferSucceeded(TransferEvent event) { }

            @Override
            public void transferFailed(TransferEvent event) { }
        });
        
        // TODO: Log repository changes
        session.setRepositoryListener(new AbstractRepositoryListener() {
            @Override
            public void artifactResolved(RepositoryEvent event) { }
        });
        
    }

    public RepositorySystem getRepositorySystem() {
        return system;
    }
    
    public DefaultRepositorySystemSession getRepositorySession() {
        return session;
    }
    
    public List<RemoteRepository> getRepositories() {
        final List<RemoteRepository> list = MavenPresets.getPresetRepositories().stream()
                .filter(r -> !onlyReleases ||
                        (onlyReleases && !r.getId().toUpperCase(Locale.ROOT).contains("SNAPSHOT")))
                .map(this::createRepository)
                .collect(Collectors.toList());
        list.addAll(PreferencesController.getSingleton().getRepositoryPreferences().getRepositories().stream()
                .filter(r -> !onlyReleases ||
                        (onlyReleases && !r.getId().toUpperCase(Locale.ROOT).contains("SNAPSHOT")))
                .map(this::createRepository)
                .collect(Collectors.toList()));
        return list;
    }
    
    public RemoteRepository getRemoteRepository(Version version) {
        if (rangeResult == null || version == null) {
            return null;
        }
        return getRepositories()
                .stream()
                .filter(r -> r.getId().equals(rangeResult.getRepository(version).getId()))
                .findFirst()
                .orElse(new RemoteRepository
                        .Builder(MavenPresets.LOCAL, "default", session.getLocalRepository().getBasedir().getAbsolutePath())
                        .build());
    }
    
    public RemoteRepository getRemoteRepository(String name) {
        return getRepositories()
                .stream()
                .filter(r -> r.getId().equals(name))
                .findFirst()
                .orElse(new RemoteRepository
                        .Builder(MavenPresets.LOCAL, "default", session.getLocalRepository().getBasedir().getAbsolutePath())
                        .build());
    }
    
    public List<Version> findVersions(Artifact artifact) {
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(getRepositories());
        try {
            rangeResult = system.resolveVersionRange(session, rangeRequest);
            cleanMetadata(artifact);
            
            return rangeResult.getVersions();
        } catch (VersionRangeResolutionException ex) { } 
        return new ArrayList<>();
    }
    
    public Version findLatestVersion(Artifact artifact) {
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(getRepositories());
        try {
            rangeResult = system.resolveVersionRange(session, rangeRequest);
            cleanMetadata(artifact);
            return rangeResult.getVersions()
                    .stream()
                    .filter(v -> !v.toString().toLowerCase(Locale.ROOT).contains("snapshot"))
                    .sorted((v1, v2) -> v2.compareTo(v1))
                    .findFirst()
                    .orElse(null);
        } catch (VersionRangeResolutionException ex) { } 
        return null;
    }
    
    private void cleanMetadata(Artifact artifact) {
        final String path = localRepo.getBasedir().getAbsolutePath() + File.separator
                        + artifact.getGroupId().replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator
                        + artifact.getArtifactId() + File.separator;
        final DefaultMetadata metadata = new DefaultMetadata("maven-metadata.xml", Metadata.Nature.RELEASE);
        getRepositories()
            .stream()
            .map(r -> session.getLocalRepositoryManager().getPathForRemoteMetadata(metadata, r, ""))
            .forEach(s -> {
                File file = new File(path + s);
                if (file.exists()) {
                    try {
                        Files.delete(file.toPath());
                        Files.delete(new File(path + s + ".sha1").toPath());
                    } catch (IOException ex) { }
                }
            });
    }
        
    public String resolveArtifacts(RemoteRepository remoteRepository, Artifact... artifact) {
        
        LocalRepository localTmpRepo = new LocalRepository(AppPlatform.getTempM2Repository());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localTmpRepo));
        
        List<Artifact> artifacts = Stream.of(artifact)
                .map(a -> {
                    ArtifactRequest artifactRequest = new ArtifactRequest();
                    artifactRequest.setArtifact(a);
                    artifactRequest.setRepositories(remoteRepository == null ? getRepositories() : 
                            Arrays.asList(remoteRepository));
                    return artifactRequest;
                })
                .map(ar -> {
                    try {
                        ArtifactResult result = system.resolveArtifact(session, ar);
                        return result.getArtifact();
                    } catch (ArtifactResolutionException ex) { }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()); 
        
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        
        List<File> sha1Files = null;
        if (artifacts != null && !artifacts.isEmpty()) {
            sha1Files = artifacts.stream()
                .filter(a -> a.getFile() != null)
                .map(a -> new File(a.getFile().getAbsolutePath().concat(".sha1")))
                .collect(Collectors.toList());
        
            InstallRequest installRequest = new InstallRequest();
            installRequest.setArtifacts(artifacts);
            try {
                system.install(session, installRequest);
            } catch (InstallationException ex) { }
        } 

        // return path from local m2
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact[0]);
        String absolutePath = "";
        try {
            final File jarFile = system.resolveArtifact(session, artifactRequest).getArtifact().getFile();
            absolutePath = jarFile.getAbsolutePath();
            if (sha1Files != null) {
                sha1Files.stream()
                    .forEach(f -> {
                        try {
                            FileUtils.copyFile(f, new File(jarFile.getParent() + File.separator + f.getName()));
                        } catch (IOException ex) { }
                    });
            }
        } catch (ArtifactResolutionException ex) { }
        
        try {
            FileUtils.deleteDirectory(AppPlatform.getTempM2Repository());
        } catch (IOException ex) { }
        
        return absolutePath;
    }
        
    public String resolveDependencies(RemoteRepository remoteRepository, Artifact artifact) {
        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(remoteRepository == null ? getRepositories() : 
                Arrays.asList(remoteRepository));

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);
        try {
            List<ArtifactResult> artifactResults = system.resolveDependencies(session, dependencyRequest)
                    .getArtifactResults();
            
            return artifactResults.stream()
                    .skip(1) // exclude jar itself
                    .map(a -> a.getArtifact().getFile().getAbsolutePath())
                    .collect(Collectors.joining(":"));
        } catch (DependencyResolutionException ex) { }
        return "";
    }
    
    private RemoteRepository createRepository(Repository repository) {
        Authentication auth = null;
        if (repository.getUser() != null && !repository.getUser().isEmpty() && 
            repository.getPassword() != null && !repository.getPassword().isEmpty()) {
            auth = new AuthenticationBuilder()
                    .addUsername(repository.getUser())
                    .addPassword(repository.getPassword())
                    .build();
        }
        
        final RemoteRepository repo = new RemoteRepository
                .Builder(repository.getId() , repository.getType(), repository.getURL())
                .setSnapshotPolicy(onlyReleases ? new RepositoryPolicy(false, null, null) : new RepositoryPolicy())
                .setAuthentication(auth)
                .build();
        return repo;
    }
    
    public String validateRepository(Repository repository) {
        RemoteRepository remoteRepository = createRepository(repository);
        
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(new DefaultArtifact("test:test:1.0"));
        artifactRequest.setRepositories(Arrays.asList(remoteRepository));
        try {
            system.resolveArtifact(session, artifactRequest);
        } catch (ArtifactResolutionException ex) {
            final String rootCauseMessage = ExceptionUtils.getRootCauseMessage(ex);
            if (rootCauseMessage != null && !rootCauseMessage.contains("ArtifactNotFoundException")) {
                return rootCauseMessage;
            }
        }
        
        return "";
    }
    
}
