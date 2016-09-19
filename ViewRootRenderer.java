package com.gregorbyte.xsp.ViewRootRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.complex.Attr;
import com.ibm.xsp.component.FacesAttrsObject;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.renderkit.html_basic.ViewRootRendererEx2;
import com.ibm.xsp.resource.LinkResource;
import com.ibm.xsp.resource.Resource;
import com.ibm.xsp.resource.ScriptResource;
import com.ibm.xsp.resource.StyleSheetResource;

public class ViewRootRenderer extends ViewRootRendererEx2 {

	@Override
	protected void encodeResourcesList(FacesContext context,
			UIViewRootEx viewRoot, ResponseWriter writer,
			List<Resource> resources) throws IOException {

		// Create 3 Lists of Resources
		List<Resource> before = new ArrayList<Resource>();
		List<Resource> normal = new ArrayList<Resource>();
		List<Resource> after = new ArrayList<Resource>();

		// Process the resources into our new lists
		for (Resource resource : resources) {

			String position = getPosition(resource);

			if (StringUtil.equals(position, "before")) {
				before.add(resource);
			} else if (StringUtil.equals(position, "after")) {
				after.add(resource);
			} else {
				normal.add(resource);
			}

		}

		// Write a comment so we can see in the generated HTML we are in the
		// right spot
		writer.writeComment("Before Resources");
		writer.write("\n"); // new line

		// Encode Resources manually, note we don't pass them to the super
		// method as we don't want them to be aggregated
		for (Resource resource : before) {
			encodeResource(context, viewRoot, writer, resource);
		}

		// Write a comment so we can see in the generated HTML we are in the
		// right spot
		writer.writeComment("Start Normal Resources");
		writer.write("\n"); // new line
		super.encodeResourcesList(context, viewRoot, writer, normal);

		// Write a comment so we can see in the generated HTML we are in the
		// right spot
		writer.writeComment("After Resources");
		writer.write("\n"); // new line

		// Encode Resources manually, note we don't pass them to the super
		// method as we don't want them to be aggregated
		for (Resource resource : after) {
			encodeResource(context, viewRoot, writer, resource);
		}

	}

	/*
	 * This method will inspect a Resource and return whether it should be on
	 * top or bottom or normal (null)
	 */
	private String getPosition(Resource resource) {

		// Your logic for choosing position goes here!

		// This test is to see if the resource has a child 'attr' tag which
		// specifies the encode-position of before or aftre
		if (resource instanceof FacesAttrsObject) {

			FacesAttrsObject o = (FacesAttrsObject) resource;

			if (o.getAttrs() != null) {

				for (Attr attr : o.getAttrs()) {

					if (StringUtil.equals(attr.getName(), "encode-position")) {
						return attr.getValue();
					}
				}

			}

		}
		
		if (resource instanceof ScriptResource) {

			// Cast it to be a ScriptResource so we can access it as such
			ScriptResource sr = (ScriptResource) resource;

			String type = sr.getType();

			if (StringUtil.isNotEmpty(type)) {

				if (type.endsWith("/before")) {

					// Fix Up the Type
					sr.setType(type.replace("/before", ""));

					// Add an attribute instead so that next time it is check it
					// will use that method
					sr.addAttr(new Attr("encode-position", "before"));

					return "before";
					
				} else if (type.endsWith("/after")) {

					// Fix Up the Type
					sr.setType(type.replace("/after", ""));

					// Add an attribute instead so that next time it is check it
					// will use that method
					sr.addAttr(new Attr("encode-position", "after"));

					return "after";

					
				}

			}

		} else if (resource instanceof StyleSheetResource) {

			StyleSheetResource ssr = (StyleSheetResource) resource;
			// You could do the same type of thing here for stylesheet

		} else if (resource instanceof LinkResource) {

			LinkResource lr = (LinkResource) resource;		
			// You could do the same type of thing here for Link Resource
			//maybe even use something like title or Styleclass instead			
			//lr.getStyleClass();
			//lr.getTitle();

		}

		// None of our tests matched so we just return null
		return null;

	}

}
