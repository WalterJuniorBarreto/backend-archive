package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.CategoryRequest;
import com.ecommerce.api_geek_store.api.dto.CategoryResponse;
import com.ecommerce.api_geek_store.api.mapper.CategoryMapper;
import com.ecommerce.api_geek_store.domain.model.Category;
import com.ecommerce.api_geek_store.domain.model.enums.CategoryStatus;
import com.ecommerce.api_geek_store.domain.repository.CategoryRepository;
import com.ecommerce.api_geek_store.exception.DuplicateResourceException;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias para CategoryServiceImpl")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private  CategoryServiceImpl categoryService;

    @Nested
    @DisplayName("Pruebas del metodo findAll")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar una pagina de CategoryResponse cuando existen filtros")
        void shouldReturnPagedCategoriesWIthFilters(){
            String searchTerm = "Soccer";
            CategoryStatus status = CategoryStatus.ACTIVO;
            Pageable pageable = PageRequest.of(0, 10);

            Category category = Category.builder().id(1L).nombre("Soccer").status(CategoryStatus.ACTIVO).build();
            Page<Category> categoryPage = new PageImpl<>(List.of(category));
            CategoryResponse response = new CategoryResponse(1L, "Soccer", CategoryStatus.ACTIVO);

            when(categoryRepository.findCategoriesWithFilters(searchTerm, status, pageable)).thenReturn(categoryPage);
            when(categoryMapper.toResponse(category)).thenReturn(response);

            Page<CategoryResponse> result = categoryService.findAll(searchTerm, status, pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).nombre()).isEqualTo("Soccer");
            verify(categoryRepository).findCategoriesWithFilters(searchTerm, status, pageable);
        }
    }

    @Nested
    @DisplayName("Pruebas del metodo findById")
    class FindByIdTests {
        @Test
        @DisplayName("Debe retornar CategoryResponse cuando el ID existe")
        void shouldReturnCategoryWhenIdExists() {
            Long id = 1L;
            Category category = Category.builder().id(id).nombre("Running").status(CategoryStatus.ACTIVO).build();
            CategoryResponse response = new CategoryResponse(id, "Running", CategoryStatus.ACTIVO);

            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
            when(categoryMapper.toResponse(category)).thenReturn(response);

            CategoryResponse result = categoryService.findById(id);


            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.nombre()).isEqualTo("Running");
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundEXception cuando el ID no existe")
        void shouldThrowExceptionWhenIdDoesNotExists(){
            Long id = 99L;

            when(categoryRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.findById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Categoria no encontrada con id: 99");
            verify(categoryMapper, never()).toResponse(any());
        }
    }

    @Nested
    @DisplayName("Pruebas del metodo create")
    class CreateTests {

        @Test
        @DisplayName("Debe guardar exitosamente una nueva categoria")
        void shouldCreateCategorySuccessfully() {
            CategoryRequest request = new CategoryRequest("Basketball");
            Category entityInput = Category.builder().nombre("Basketball").build();
            Category entitySaved = Category.builder().id(1L).nombre("Basketball").status(CategoryStatus.ACTIVO).build();
            CategoryResponse response = new CategoryResponse(1L, "Basketball", CategoryStatus.ACTIVO);

            when(categoryRepository.existsByNombreIgnoreCase(request.nombre())).thenReturn(false);
            when(categoryMapper.toEntity(request)).thenReturn(entityInput);
            when(categoryRepository.save(entityInput)).thenReturn(entitySaved);
            when(categoryMapper.toResponse(entitySaved)).thenReturn(response);


            CategoryResponse result = categoryService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            verify(categoryRepository).save(entityInput);
        }

        @Test
        @DisplayName("Debe lanzar DUplicateResourceException si el nombre ya existe")
        void shouldThrowExceptionWhenCategoryNameExits(){
            CategoryRequest request = new CategoryRequest("Running");

            when(categoryRepository.existsByNombreIgnoreCase(request.nombre())).thenReturn(true);

            assertThatThrownBy(()-> categoryService.create(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Ya existe una categoria con el nombre Running");
            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas del metodo update")
    class UpdateTests {

        @Test
        @DisplayName("Debe actualizar el nombre de la categoria correctamente")
        void shouldUpdateCategoryNameSuccessfully(){
            Long id = 1L;
            CategoryRequest request = new CategoryRequest("Skate");
            Category existingCategory = Category.builder().id(id).nombre("old").status(CategoryStatus.ACTIVO).build();
            CategoryResponse response = new CategoryResponse(id, "Skate", CategoryStatus.ACTIVO);

            when(categoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.existsByNombreIgnoreCase(request.nombre())).thenReturn(false);
            when(categoryMapper.toResponse(existingCategory)).thenReturn(response);

            CategoryResponse result = categoryService.update(id, request);

            assertThat(result.nombre()).isEqualTo("Skate");
            assertThat(existingCategory.getNombre()).isEqualTo("Skate");
        }


        @Test
        @DisplayName("Debe lanzar DuplicateResourceEXcepetion si el nuevo nombre ya le pertenece a otra categoira")
        void shouldThrowExceptionWhenUpdatedNameConflicts(){
            Long id = 1L;
            CategoryRequest request = new CategoryRequest("Soccer");
            Category existingCategory = Category.builder().id(id).nombre("Running").status(CategoryStatus.ACTIVO).build();

            when(categoryRepository.findById(id)).thenReturn(Optional.of(existingCategory));
            when(categoryRepository.existsByNombreIgnoreCase(request.nombre())).thenReturn(true);

            assertThatThrownBy(()-> categoryService.update(id, request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Ya existe otra categoria con el nombre: Soccer");
        }
    }


    @Nested
    @DisplayName("PRuebas del metodo deleteByID soft delete")
    class DeleteByIdTests{

        @Test
        @DisplayName("Debe cambiar el estado a INACTIVO si la categoria estaba activa")
        void shouldDeactivateCategorySuccessfully(){
            Long id = 1L;
            Category category = Category.builder().id(id).nombre("Running").status(CategoryStatus.ACTIVO).build();

            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

            categoryService.deleteById(id);

            assertThat(category.getStatus()).isEqualTo(CategoryStatus.INACTIVO);
        }

        @Test
        @DisplayName("NO debe realizar modificaciones si la categoria ya estaba INACTIVA")
        void shouldDoNothingIfCategoryIsAlreadyInactive(){
            Long id = 1L;
            Category category = Category.builder().id(id).nombre("Running").status(CategoryStatus.INACTIVO).build();

            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

            categoryService.deleteById(id);

            assertThat(category.getStatus()).isEqualTo(CategoryStatus.INACTIVO);
        }
    }


    @Nested
    @DisplayName("Pruebas del metodo activate")
    class ActivateTests {

        @Test
        @DisplayName("Debe cambiar el estado a ACTIVO si la categoria estaba inactiva")
        void shouldActivateCategorySuccessfully(){
            Long id = 1L;
            Category category = Category.builder().id(id).nombre("Running").status(CategoryStatus.INACTIVO).build();
            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

            categoryService.activate(id);

            assertThat(category.getStatus()).isEqualTo(CategoryStatus.ACTIVO);
        }


        @Test
        @DisplayName("No debe realizar modificaciones si la categoría ya estaba ACTIVA (Idempotencia)")
        void shouldDoNothingIfCategoryIsAlreadyActive() {
            Long id = 1L;
            Category category = Category.builder().id(id).nombre("Running").status(CategoryStatus.ACTIVO).build();
            when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

            categoryService.activate(id);

            assertThat(category.getStatus()).isEqualTo(CategoryStatus.ACTIVO);
        }
    }
}
