package ch.store.api.admin;

import java.net.URI;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.store.api.domain.Category;
import ch.store.api.domain.CategoryTree;

/**
 * The category API endpoints.
 * @author: B. Kanli
 *
 */
@Path("/")
public class CategoryResource {

	@PersistenceContext(unitName = "AdminPU")
	private EntityManager em;

	/**
	 * Retrieves all categories from the db store.
	 * @return a list of categories
	 * @throws Exception if backend not accessible
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Category> all() throws Exception {
		return em.createNamedQuery("Category.findAll", Category.class).getResultList();
	}

	/**
	 * Creates the category tree.
	 * @return the tree
	 * @throws Exception if backend not accessible
	 */
	@GET
	@Path("/tree")
	@Produces(MediaType.APPLICATION_JSON)
	public CategoryTree tree() throws Exception {
		return em.find(CategoryTree.class, 1);
	}

	/**
	 * Creates a new {@link Category}.
	 * @param category the category to create
	 * @return the response
	 * @throws Exception if creation fails
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response create(Category category) throws Exception {
		if (category.getId() != null) {
			return Response.status(Response.Status.CONFLICT).entity("Unable to create Category, id was already set.")
					.build();
		}

		Category parent;
		if ((parent = category.getParent()) != null && parent.getId() != null) {
			category.setParent(get(parent.getId()));
		}

		try {
			em.persist(category);
			em.flush();
		} catch (ConstraintViolationException cve) {
			return Response.status(Response.Status.BAD_REQUEST).entity(cve.getMessage()).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
		return Response.created(new URI("category/" + category.getId().toString())).build();
	}

	/**
	 * Finds the category by its id.
	 * @param categoryId the category id
	 * @return the category to be found
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{categoryId}")
	public Category get(@PathParam("categoryId") Integer categoryId) {
		return em.find(Category.class, categoryId);
	}

	/**
	 * Deletes a category with the given id.
	 * @param categoryId the category id
	 * @return the response
	 * @throws Exception if deletion fails
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{categoryId}")
	@Transactional
	public Response remove(@PathParam("categoryId") Integer categoryId) throws Exception {
		try {
			Category entity = em.find(Category.class, categoryId);
			em.remove(entity);
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}

		return Response.noContent().build();
	}

	/**
	 * Modifies the category with the given id.
	 * @param categoryId the category id
	 * @param category the category
	 * @return the response
	 * @throws Exception if update fails
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{categoryId}")
	@Transactional
	public Response update(@PathParam("categoryId") Integer categoryId, Category category) throws Exception {
		try {
			Category entity = em.find(Category.class, categoryId);

			if (null == entity) {
				return Response.status(Response.Status.NOT_FOUND)
						.entity("Category with id of " + categoryId + " does not exist.").build();
			}
			Category parent;
			if ((parent = category.getParent()) != null) {
				if (parent.getId() != null && parent.getVersion() == null) {
					category.setParent(get(parent.getId()));
				}
			}
			em.merge(category);

			return Response.ok(category).build();
		} catch (Exception e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
}
