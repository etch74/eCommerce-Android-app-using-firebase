package com.example.ecommerceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerceapp.Currents.Current;
import com.example.ecommerceapp.Model.Cart;
import com.example.ecommerceapp.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button nextbtn;
    private TextView txtTotal;

    private int TotalPrice=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        recyclerView=findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        nextbtn=(Button) findViewById(R.id.next_btn);
        txtTotal=(TextView) findViewById(R.id.total_price);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtTotal.setText("Total Price= "+String.valueOf(TotalPrice));
                Intent intent= new Intent(CartActivity.this , ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price",String.valueOf(TotalPrice));
                startActivity(intent);
                finish();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        txtTotal.setText("Total Price= "+String.valueOf(TotalPrice));
        final DatabaseReference cartListRef= FirebaseDatabase.getInstance().getReference().child("Cart List");
        FirebaseRecyclerOptions<Cart>options=
                new FirebaseRecyclerOptions.Builder<Cart>()
                        .setQuery(cartListRef.child("User View")
                                .child(Current.currentOnlineUser.getPhone())
                                .child("Products"),Cart.class)
                        .build();
        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter
                =new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, int i, @NonNull Cart cart)
            {
                cartViewHolder.txtProductQuantity.setText("Quantity= "+cart.getQuantity());
                cartViewHolder.txtProductName.setText(cart.getPname());
                cartViewHolder.txtProductPrice.setText("Price= "+cart.getPrice()+"L.E");
                int oneTypeProductPrice=((Integer.valueOf(cart.getPrice())))* Integer.valueOf(cart.getQuantity());
                TotalPrice=TotalPrice+oneTypeProductPrice;
                cartViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                      CharSequence options[]= new CharSequence[]
                              {
                                      "Edit",
                                      "Remove"
                              };
                        AlertDialog.Builder builder= new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options: ");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(i==0)
                                {
                                    Intent intent = new Intent(CartActivity.this , ProductDetailsActivity.class);
                                    intent.putExtra("pid" , cart.getPid());
                                    startActivity(intent);
                                }
                                if(i==1)
                                {
                                    cartListRef.child("User View")
                                            .child(Current.currentOnlineUser.getPhone())
                                            .child("Products").child(cart.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(CartActivity.this , "Item is removed" , Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(CartActivity.this , HomeActivity2.class);

                                                        startActivity(intent);
                                                    }

                                                }
                                            });
                                }
                            }
                        });
                        builder.show();
                    }
                });

            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout , parent , false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }
}