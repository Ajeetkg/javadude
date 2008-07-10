/*******************************************************************************
 *  Copyright 2008 Scott Stanchfield.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package com.javadude.antxr.eclipse.core.builder;


/**
 * Runs "Organize Imports" on generated code
 */
public class ImportOrganizer {
//	static final class OrganizeImportError extends RuntimeException {
//		private static final long serialVersionUID= 1L;
//	}
//
//	private boolean testOnBuildPath(ICompilationUnit cu, MultiStatus status) {
//		IJavaProject project= cu.getJavaProject();
//		if (!project.isOnClasspath(cu)) {
//			String cuLocation= cu.getPath().makeRelative().toString();
//			String message= ActionMessages.getFormattedString("OrganizeImportsAction.multi.error.notoncp", cuLocation); //$NON-NLS-1$
//			status.add(new Status(IStatus.INFO, JavaUI.ID_PLUGIN, IStatus.ERROR, message, null));
//			return false;
//		}
//		return true;
//	}
//
//	private void runInSync(final OrganizeImportsOperation op, final String cuLocation, final MultiStatus status, final IProgressMonitor monitor) {
//		Runnable runnable= new Runnable() {
//			public void run() {
//				try {
//					op.run(new SubProgressMonitor(monitor, 1));
//				} catch (ValidateEditException e) {
//					status.add(e.getStatus());
//				} catch (CoreException e) {
//					AntxrCorePlugin.log(e);
//					String message= ActionMessages.getFormattedString("OrganizeImportsAction.multi.error.unexpected", e.getStatus().getMessage()); //$NON-NLS-1$
//					status.add(new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR, message, null));					
//				} catch (OrganizeImportError e) {
//					String message= ActionMessages.getFormattedString("OrganizeImportsAction.multi.error.unresolvable", cuLocation); //$NON-NLS-1$
//					status.add(new Status(IStatus.INFO, JavaUI.ID_PLUGIN, IStatus.ERROR, message, null));
//				} catch (OperationCanceledException e) {
//					// cancelled
//					monitor.setCanceled(true);
//				}
//			}
//		};
//		Display.getDefault().syncExec(runnable);
//	}
//
//	
//	/**
//	 * Organize imports on generated compilation units
//	 * @param compilationUnitFiles The compilation units to process
//	 * @param status A collective status indicator
//	 * @param monitor a progress monitor
//	 * @throws OperationCanceledException user canceled
//	 */
//	public void organizeImports(List compilationUnitFiles, MultiStatus status, IProgressMonitor monitor) throws OperationCanceledException {
//		if (monitor == null) {
//			monitor= new NullProgressMonitor();
//		}	
//		monitor.setTaskName(ActionMessages.getString("OrganizeImportsAction.multi.op.description")); //$NON-NLS-1$
//	
//		monitor.beginTask("", compilationUnitFiles.size()); //$NON-NLS-1$
//		try {
//			IChooseImportQuery query= new IChooseImportQuery() {
//				public TypeInfo[] chooseImports(TypeInfo[][] openChoices, ISourceRange[] ranges) {
//					throw new OrganizeImportError();
//				}
//			};
//			IJavaProject lastProject= null;
//			
//			for (Iterator i = compilationUnitFiles.iterator(); i
//					.hasNext();) {
//				IFile file = (IFile)i.next();
//				ICompilationUnit cu = (ICompilationUnit)JavaCore.create(file);
//				if (testOnBuildPath(cu, status)) {
//					if (lastProject == null || !lastProject.equals(cu.getJavaProject())) {
//						lastProject= cu.getJavaProject();
//					}
//					CodeGenerationSettings settings= JavaPreferencesSettings.getCodeGenerationSettings(lastProject);
//
//					
//					String cuLocation= cu.getPath().makeRelative().toString();
//					
//					monitor.subTask(cuLocation);
//
//					try {
//						boolean save= !cu.isWorkingCopy();
//						if (!save) {
//							ITextFileBuffer textFileBuffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(cu.getPath());
//							save= textFileBuffer != null && !textFileBuffer.isDirty(); // save when not dirty
//						}
//						
//						OrganizeImportsOperation op= new OrganizeImportsOperation(cu, settings.importOrder, settings.importThreshold, settings.importIgnoreLowercase, save, true, query);
//						runInSync(op, cuLocation, status, monitor);
//
//						IProblem parseError= op.getParseError();
//						if (parseError != null) {
//							String message= ActionMessages.getFormattedString("OrganizeImportsAction.multi.error.parse", cuLocation); //$NON-NLS-1$
//							status.add(new Status(IStatus.INFO, JavaUI.ID_PLUGIN, IStatus.ERROR, message, null));
//						} 	
//					} catch (CoreException e) {
//						AntxrCorePlugin.log(e);
//						String message= ActionMessages.getFormattedString("OrganizeImportsAction.multi.error.unexpected", e.getStatus().getMessage()); //$NON-NLS-1$
//						status.add(new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR, message, null));					
//					}
//
//					if (monitor.isCanceled()) {
//						throw new OperationCanceledException();
//					}
//				}
//			}
//		} finally {
//			monitor.done();
//		}
//	}	
}
