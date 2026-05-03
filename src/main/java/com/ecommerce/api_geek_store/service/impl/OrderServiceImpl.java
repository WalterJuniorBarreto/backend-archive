package com.ecommerce.api_geek_store.service.impl;



import com.cloudinary.Cloudinary;

import com.cloudinary.utils.ObjectUtils;

import com.ecommerce.api_geek_store.api.dto.*;

import com.ecommerce.api_geek_store.api.mapper.OrderMapper;

import com.ecommerce.api_geek_store.domain.model.*;

import com.ecommerce.api_geek_store.domain.repository.OrderRepository;

import com.ecommerce.api_geek_store.domain.repository.ProductRepository;

import com.ecommerce.api_geek_store.domain.repository.UserRepository;

import com.ecommerce.api_geek_store.exception.InsufficientStockException;

import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;

import com.ecommerce.api_geek_store.service.OrderService;

import com.ecommerce.api_geek_store.service.notification.EmailService;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.*;

import java.util.stream.Collectors;



@Service

@Transactional

public class OrderServiceImpl implements OrderService {



    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);



    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final OrderMapper orderMapper;

    private final EmailService emailService;

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder.root}")

    private String rootFolder;



    public OrderServiceImpl(OrderRepository orderRepository,

                            ProductRepository productRepository,

                            UserRepository userRepository,

                            OrderMapper orderMapper,

                            EmailService emailService,

                            Cloudinary cloudinary) {

        this.orderRepository = orderRepository;

        this.productRepository = productRepository;

        this.userRepository = userRepository;

        this.orderMapper = orderMapper;

        this.emailService = emailService;

        this.cloudinary = cloudinary;

    }



    @Override

    @Transactional

    public OrderResponse createOrder(OrderRequest orderRequest, UserDetails userDetails) {

        return null;

    }



    @Override

    @Transactional(readOnly = true)

    public List<OrderResponse> findMyOrders(UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())

                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userDetails.getUsername()));



        return orderRepository.findByUserId(user.getId()).stream()

                .map(orderMapper::toOrderResponse)

                .collect(Collectors.toList());

    }



    @Override

    @Transactional(readOnly = true)

    public List<OrderResponse> findAllOrders() {

        return orderRepository.findAll().stream()

                .map(orderMapper::toOrderResponse)

                .collect(Collectors.toList());

    }



    @Override

    @Transactional

    public OrderResponse updateOrderStatus(Long orderId, String status) {

        Order order = orderRepository.findById(orderId)

                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));



        String oldStatus = order.getEstado();

        String newStatus = status.toUpperCase();



        order.setEstado(newStatus);

        Order savedOrder = orderRepository.save(order);



        log.info("Orden ID {} actualizada: {} -> {}", orderId, oldStatus, newStatus);



        if (!oldStatus.equals(newStatus)) {

            final String userEmail = savedOrder.getUser().getEmail();

            emailService.sendOrderStatusUpdate(userEmail, savedOrder);

        }



        return orderMapper.toOrderResponse(savedOrder);

    }



    @Override

    @Transactional

    public void createOrderFromPayment(

            String email,

            List<PaymentRequest.PaymentItem> itemsRequest,

            PaymentRequest.PaymentAddress dirRequest,

            BigDecimal totalPaid

    ) {

        User user = userRepository.findByEmail(email)

                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));



        log.info("Procesando orden pagada para: {}", email);



        Order order = new Order();

        order.setUser(user);

        order.setEstado("PAGADO");

        order.setTotal(totalPaid);

        order.setFechaCreacion(LocalDateTime.now());

        order.setMetodoPago("MERCADO_PAGO");



        ShippingAddress addressEntity = new ShippingAddress(

                dirRequest.calle(),

                dirRequest.ciudad(),

                dirRequest.estado(),

                dirRequest.codigoPostal(),

                dirRequest.pais()

        );

        order.setEnvio(addressEntity);



        Set<OrderItem> orderItems = new HashSet<>();



        for (PaymentRequest.PaymentItem itemReq : itemsRequest) {

            Product product = productRepository.findById(itemReq.productId())

                    .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + itemReq.productId() + " no encontrado"));



            ProductVariant variant = product.getVariants().stream()

                    .filter(v -> v.getId().equals(itemReq.variantId()))

                    .findFirst()

                    .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada"));



            if (variant.getStock() < itemReq.cantidad()) {

                throw new InsufficientStockException("Sin stock para: " + product.getNombre());

            }

            variant.setStock(variant.getStock() - itemReq.cantidad());



            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);

            orderItem.setProduct(product);

            orderItem.setCantidad(itemReq.cantidad());

            orderItem.setPrecioUnitario(product.getPrecio());

            orderItem.setNombreProducto(product.getNombre());

            orderItem.setColor(variant.getColor());

            orderItem.setTalla(variant.getTalla());



            orderItems.add(orderItem);

        }



        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);



        log.info("ORDEN GUARDADA EXITOSAMENTE: ID {}", savedOrder.getId());



        emailService.sendOrderConfirmation(email, savedOrder);

    }



    @Override

    @Transactional

    public OrderResponse addTrackingInfo(Long orderId, String trackingNumber, String courierName) {

        Order order = orderRepository.findById(orderId)

                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));



        order.setTrackingNumber(trackingNumber);

        order.setCourierName(courierName);



        if ("PAGADO".equals(order.getEstado()) || "PENDIENTE".equals(order.getEstado())) {

            order.setEstado("ENVIADO");

        }



        Order savedOrder = orderRepository.save(order);

        log.info("Tracking agregado a orden {}: {}", orderId, trackingNumber);



        emailService.sendOrderStatusUpdate(savedOrder.getUser().getEmail(), savedOrder);



        return orderMapper.toOrderResponse(savedOrder);

    }



    @Override

    @Transactional

    public OrderResponse createManualOrder(OrderRequest req, MultipartFile file, UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())

                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));



        Order order = new Order();

        order.setUser(user);

        order.setFechaCreacion(LocalDateTime.now());

        order.setMetodoPago("YAPE_QR");

        order.setEstado("POR_CONFIRMAR");



        if (req.direccion() != null) {

            order.setEnvio(new ShippingAddress(

                    req.direccion().calle(),

                    req.direccion().ciudad(),

                    req.direccion().estado(),

                    req.direccion().codigoPostal(),

                    req.direccion().pais()

            ));

        }



        Set<OrderItem> orderItems = new HashSet<>();

        BigDecimal totalOrder = BigDecimal.ZERO;

        BigDecimal costoEnvio = BigDecimal.ZERO;

        BigDecimal tarifaEnvio = new BigDecimal("20.00");



        for (OrderItemRequest itemReq : req.items()) {

            Product product = productRepository.findById(itemReq.productId())

                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));



            ProductVariant variant = product.getVariants().stream()

                    .filter(v -> v.getId().equals(itemReq.variantId()))

                    .findFirst()

                    .orElseThrow(() -> new ResourceNotFoundException("Variante no encontrada"));



            if (variant.getStock() < itemReq.cantidad()) {

                throw new InsufficientStockException("Stock insuficiente: " + product.getNombre());

            }

            variant.setStock(variant.getStock() - itemReq.cantidad());



            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);

            orderItem.setProduct(product);

            orderItem.setCantidad(itemReq.cantidad());

            orderItem.setPrecioUnitario(product.getPrecio());

            orderItem.setNombreProducto(product.getNombre());

            orderItem.setColor(variant.getColor());

            orderItem.setTalla(variant.getTalla());



            orderItems.add(orderItem);



            totalOrder = totalOrder.add(product.getPrecio().multiply(BigDecimal.valueOf(itemReq.cantidad())));

            costoEnvio = costoEnvio.add(tarifaEnvio.multiply(BigDecimal.valueOf(itemReq.cantidad())));

        }



        order.setItems(orderItems);

        order.setTotal(totalOrder.add(costoEnvio));



        if (req.codOperacion() != null && !req.codOperacion().isEmpty()) {

            order.setCodOperacion(req.codOperacion());

        }



        if (file != null && !file.isEmpty()) {

            try {



                String carpetaDestino = rootFolder + "/comprobantes";



                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(

                        "folder", carpetaDestino

                ));



                String url = uploadResult.get("secure_url").toString();

                order.setUrlComprobante(url);

                log.info("Comprobante subido a: {} -> {}", carpetaDestino, url);



            } catch (IOException e) {

                log.error("Error al subir comprobante a Cloudinary", e);

                throw new RuntimeException("Error al subir el comprobante de pago");

            }

        }



        Order savedOrder = orderRepository.save(order);



        emailService.sendOrderConfirmation(user.getEmail(), savedOrder);


        return orderMapper.toOrderResponse(savedOrder);

    }

}