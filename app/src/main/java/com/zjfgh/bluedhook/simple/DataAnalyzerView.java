// RecyclerView适配器
private class RecordAdapter extends RecyclerView.Adapter<RecordViewHolder> {
    private List<RecordItem> records;

    public RecordAdapter(List<RecordItem> records) {
        this.records = new ArrayList<>(); // 初始化为空列表，防止 NullPointerException
    }

    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 返回一个空的 ViewHolder
        return new RecordViewHolder(new View(parent.getContext()), this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        // 不绑定任何数据
    }

    @Override
    public int getItemCount() {
        return records.size(); // 返回默认数量
    }
}

// 数据模型类
public static class RecordItem {
    // 保留字段定义，方便未来恢复使用
    public String time;
    public String giftType;
    public String user;
    public String gift;
    public String beans;
    public String count;
    public String total;
    public String toAnchor;
}

// ViewHolder类
private class RecordViewHolder extends RecyclerView.ViewHolder {
    private final RecordAdapter adapter;
    private final View parentView;

    public RecordViewHolder(@NonNull View itemView, RecordAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        this.parentView = itemView;
    }

    public void bind(RecordItem item, int position) {
        // 不执行任何绑定逻辑
    }
}
